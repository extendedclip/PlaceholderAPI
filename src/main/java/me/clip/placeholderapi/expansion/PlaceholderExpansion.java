/*
 * This file is part of PlaceholderAPI
 *
 * PlaceholderAPI
 * Copyright (c) 2015 - 2020 PlaceholderAPI Team
 *
 * PlaceholderAPI free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlaceholderAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.clip.placeholderapi.expansion;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class PlaceholderExpansion
{

	/**
	 * The placeholder identifier of this expansion
	 *
	 * @return placeholder identifier that is associated with this expansion
	 */
	@NotNull
	public abstract String getIdentifier();

	/**
	 * The author of this expansion
	 *
	 * @return name of the author for this expansion
	 */
	@NotNull
	public abstract String getAuthor();

	/**
	 * The version of this expansion
	 *
	 * @return current version of this expansion
	 */
	@NotNull
	public abstract String getVersion();

	@Nullable
	public abstract String onRequest(@Nullable final OfflinePlayer player, @NotNull final String params);


	/**
	 * The name of this expansion
	 *
	 * @return {@link #getIdentifier()} by default, name of this expansion if specified
	 */
	@NotNull
	public String getName()
	{
		return getIdentifier();
	}

	/**
	 * The name of the plugin that this expansion hooks into. by default will null
	 *
	 * @return plugin name that this expansion requires to function
	 */
	@Nullable
	public String getRequiredPlugin()
	{
		return getPlugin();
	}

	/**
	 * The placeholders associated with this expansion
	 *
	 * @return placeholder list that this expansion provides
	 */
	@NotNull
	public List<String> getPlaceholders()
	{
		return Collections.emptyList();
	}


	/**
	 * Expansions that do not use the ecloud and instead register from the dependency should set this
	 * to true to ensure that your placeholder expansion is not unregistered when the papi reload
	 * command is used
	 *
	 * @return if this expansion should persist through placeholder reloads
	 */
	public boolean persist()
	{
		return false;
	}


	/**
	 * Check if this placeholder identifier has already been registered
	 *
	 * @return true if the identifier for this expansion is already registered
	 */
	public final boolean isRegistered()
	{
		return getPlaceholderAPI().getLocalExpansionManager().findExpansionByIdentifier(getIdentifier()).map(it -> it.equals(this)).orElse(false);
	}


	/**
	 * If any requirements need to be checked before this expansion should register, you can check
	 * them here
	 *
	 * @return true if this hook meets all the requirements to register
	 */
	public boolean canRegister()
	{
		return getRequiredPlugin() == null || Bukkit.getPluginManager().getPlugin(getRequiredPlugin()) != null;
	}

	/**
	 * Attempt to register this PlaceholderExpansion
	 *
	 * @return true if this expansion is now registered with PlaceholderAPI
	 * @deprecated This is going to be final in the future, startup and shutdown logic will have their own methods soon.
	 */
	@Deprecated
	public boolean register()
	{
		return canRegister() && getPlaceholderAPI().getLocalExpansionManager().register(this);
	}

	/**
	 * Attempt to unregister this PlaceholderExpansion
	 *
	 * @return true if this expansion is now unregistered with PlaceholderAPI
	 */
	public final boolean unregister()
	{
		return getPlaceholderAPI().getLocalExpansionManager().unregister(this);
	}


	/**
	 * Quick getter for the {@link PlaceholderAPIPlugin} instance
	 *
	 * @return {@link PlaceholderAPIPlugin} instance
	 */
	@NotNull
	public final PlaceholderAPIPlugin getPlaceholderAPI()
	{
		return PlaceholderAPIPlugin.getInstance();
	}


	// === Configuration ===

	@Nullable
	public final ConfigurationSection getConfigSection()
	{
		return getPlaceholderAPI().getConfig().getConfigurationSection("expansions." + getIdentifier());
	}

	@Nullable
	public final ConfigurationSection getConfigSection(@NotNull final String path)
	{
		final ConfigurationSection section = getConfigSection();
		return section == null ? null : section.getConfigurationSection(path);
	}

	@Nullable
	@Contract("_, !null -> !null")
	public final Object get(@NotNull final String path, final Object def)
	{
		final ConfigurationSection section = getConfigSection();
		return section == null ? def : section.get(path, def);
	}

	public final int getInt(@NotNull final String path, final int def)
	{
		final ConfigurationSection section = getConfigSection();
		return section == null ? def : section.getInt(path, def);
	}

	public final long getLong(@NotNull final String path, final long def)
	{
		final ConfigurationSection section = getConfigSection();
		return section == null ? def : section.getLong(path, def);
	}

	public final double getDouble(@NotNull final String path, final double def)
	{
		final ConfigurationSection section = getConfigSection();
		return section == null ? def : section.getDouble(path, def);
	}

	@Nullable
	@Contract("_, !null -> !null")
	public final String getString(@NotNull final String path, @Nullable final String def)
	{
		final ConfigurationSection section = getConfigSection();
		return section == null ? def : section.getString(path, def);
	}

	@NotNull
	public final List<String> getStringList(@NotNull final String path)
	{
		final ConfigurationSection section = getConfigSection();
		return section == null ? Collections.emptyList() : section.getStringList(path);
	}

	public final boolean configurationContains(@NotNull final String path)
	{
		final ConfigurationSection section = getConfigSection();
		return section != null && section.contains(path);
	}

	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof PlaceholderExpansion))
		{
			return false;
		}

		final PlaceholderExpansion expansion = (PlaceholderExpansion) o;

		return getIdentifier().equals(expansion.getIdentifier()) &&
			   getAuthor().equals(expansion.getAuthor()) &&
			   getVersion().equals(expansion.getVersion());
	}

	@Override
	public final int hashCode()
	{
		return Objects.hash(getIdentifier(), getAuthor(), getVersion());
	}

	@Override
	public final String toString()
	{
		return String.format("PlaceholderExpansion[name: '%s', author: '%s', version: '%s']", getName(), getAuthor(), getVersion());
	}

	// === Deprecated API ===

	/**
	 * @deprecated As of versions greater than 2.8.7, use {@link #getRequiredPlugin()}
	 */
	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "2.10.8")
	public String getPlugin()
	{
		return null;
	}

	/**
	 * @deprecated As of versions greater than 2.8.7, use the expansion cloud to show a description
	 */
	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "2.10.8")
	public String getDescription()
	{
		return null;
	}

	/**
	 * @deprecated As of versions greater than 2.8.7, use the expansion cloud to display a link
	 */
	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "2.10.8")
	public String getLink()
	{
		return null;
	}

}
