/*
 *
 * PlaceholderAPI
 * Copyright (C) 2019 Ryan McCarthy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package me.clip.placeholderapi;

import me.clip.placeholderapi.commands.PlaceholderCommandRouter;
import me.clip.placeholderapi.configuration.PlaceholderAPIConfig;
import me.clip.placeholderapi.expansion.ExpansionManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Version;
import me.clip.placeholderapi.expansion.cloud.ExpansionCloudManager;
import me.clip.placeholderapi.listeners.PlaceholderListener;
import me.clip.placeholderapi.listeners.ServerLoadEventListener;
import me.clip.placeholderapi.updatechecker.UpdateChecker;
import me.clip.placeholderapi.util.Msg;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Yes I have a shit load of work to do...
 *
 * @author Ryan McCarthy
 */
public final class PlaceholderAPIPlugin extends JavaPlugin
{

	@NotNull
	private static final Version version = resolveServerVersion();


	private static PlaceholderAPIPlugin instance;


	@NotNull
	private final PlaceholderAPIConfig  config  = new PlaceholderAPIConfig(this);
	@NotNull
	private final ExpansionCloudManager cloud   = new ExpansionCloudManager(this);
	@NotNull
	private final ExpansionManager      manager = new ExpansionManager(this);


	@Override
	public void onLoad()
	{
		instance = this;

		saveDefaultConfig();
	}

	@Override
	public void onEnable()
	{
		setupCommand();
		setupMetrics();
		setupExpansions();

		new PlaceholderListener(this);

		if (config.isCloudEnabled())
		{
			enableCloud();
		}

		if (config.checkUpdates())
		{
			new UpdateChecker(this).fetch();
		}
	}

	@Override
	public void onDisable()
	{
		disableCloud();

		PlaceholderAPI.unregisterAll();

		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);

		instance = null;
	}


	public void reloadConf(@NotNull final CommandSender sender)
	{
		PlaceholderAPI.unregisterAllProvidedExpansions();

		reloadConfig();

		manager.registerAllExpansions();

		if (config.isCloudEnabled())
		{
			enableCloud();
		}
		else
		{
			disableCloud();
		}

		Msg.msg(sender,
				PlaceholderAPI.getRegisteredIdentifiers().size() + " &aplaceholder hooks successfully registered!");
	}

	public void enableCloud()
	{
		disableCloud();
		cloud.fetch(config.cloudAllowUnverifiedExpansions());
	}

	public void disableCloud()
	{
		cloud.clean();
	}


	/**
	 * Obtain the configuration class for PlaceholderAPI.
	 *
	 * @return PlaceholderAPIConfig instance
	 */
	@NotNull
	public PlaceholderAPIConfig getPlaceholderAPIConfig()
	{
		return config;
	}

	@NotNull
	public ExpansionManager getExpansionManager()
	{
		return manager;
	}

	@NotNull
	public ExpansionCloudManager getExpansionCloud()
	{
		return cloud;
	}


	private void setupCommand()
	{
		final PluginCommand pluginCommand = getCommand("placeholderapi");
		if (pluginCommand == null)
		{
			return;
		}

		final PlaceholderCommandRouter router = new PlaceholderCommandRouter(this);
		pluginCommand.setExecutor(router);
		pluginCommand.setTabCompleter(router);
	}

	private void setupMetrics()
	{
		final Metrics metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.SimplePie("using_expansion_cloud", () -> getPlaceholderAPIConfig().isCloudEnabled() ? "yes" : "no"));

		metrics.addCustomChart(new Metrics.SimplePie("using_spigot", () -> getServerVersion().isSpigot() ? "yes" : "no"));

		metrics.addCustomChart(new Metrics.AdvancedPie("expansions_used", () -> {

			Map<String, Integer>         map   = new HashMap<>();
			Map<String, PlaceholderHook> hooks = PlaceholderAPI.getPlaceholders();

			if (!hooks.isEmpty())
			{

				for (PlaceholderHook hook : hooks.values())
				{
					if (hook instanceof PlaceholderExpansion)
					{
						PlaceholderExpansion expansion = (PlaceholderExpansion) hook;
						map.put(expansion.getRequiredPlugin() == null ? expansion.getIdentifier() : expansion.getRequiredPlugin(), 1);
					}
				}
			}

			return map;
		}));
	}

	private void setupExpansions()
	{
		try
		{
			Class.forName("org.bukkit.event.server.ServerLoadEvent");
			new ServerLoadEventListener(this);
		}
		catch (final ExceptionInInitializerError | ClassNotFoundException exception)
		{
			Bukkit.getScheduler().runTaskLater(this, getExpansionManager()::initializeExpansions, 1);
		}
	}


	/**
	 * Gets the static instance of the main class for PlaceholderAPI. This class is not the actual API
	 * class, this is the main class that extends JavaPlugin. For most API methods, use static methods
	 * available from the class: {@link PlaceholderAPI}
	 *
	 * @return PlaceholderAPIPlugin instance
	 */
	@NotNull
	public static PlaceholderAPIPlugin getInstance()
	{
		return instance;
	}


	/**
	 * Get the configurable {@linkplain SimpleDateFormat} object that is used to parse time for
	 * generic time based placeholders
	 *
	 * @return date format
	 */
	@NotNull
	public static SimpleDateFormat getDateFormat()
	{
		try
		{
			return new SimpleDateFormat(getInstance().getPlaceholderAPIConfig().dateFormat());
		}
		catch (final IllegalArgumentException ex)
		{

			getInstance().getLogger().log(Level.WARNING, "configured date format is invalid", ex);

			return new SimpleDateFormat("MM/dd/yy HH:mm:ss");
		}
	}

	/**
	 * Get the configurable {@linkplain String} value that should be returned when a boolean is true
	 *
	 * @return string value of true
	 */
	@NotNull
	public static String booleanTrue()
	{
		return getInstance().getPlaceholderAPIConfig().booleanTrue();
	}

	/**
	 * Get the configurable {@linkplain String} value that should be returned when a boolean is false
	 *
	 * @return string value of false
	 */
	@NotNull
	public static String booleanFalse()
	{
		return getInstance().getPlaceholderAPIConfig().booleanFalse();
	}


	public static Version getServerVersion()
	{
		return version;
	}

	private static Version resolveServerVersion()
	{
		final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		boolean isSpigot;
		try
		{
			Class.forName("org.spigotmc.SpigotConfig");
			isSpigot = true;
		}
		catch (final ExceptionInInitializerError | ClassNotFoundException ignored)
		{
			isSpigot = false;
		}

		return new Version(version, isSpigot);
	}

}
