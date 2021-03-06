package me.clip.placeholderapi.commands.impl.local;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public final class CommandExpansionUnregister extends PlaceholderCommand
{

	public CommandExpansionUnregister()
	{
		super("unregister");
	}

	@Override
	public void evaluate(@NotNull final PlaceholderAPIPlugin plugin, @NotNull final CommandSender sender, @NotNull final String alias, @NotNull @Unmodifiable final List<String> params)
	{
		if (params.isEmpty())
		{
			Msg.msg(sender,
					"&cYou must specify the name of the expansion.");
			return;
		}

		final PlaceholderExpansion expansion = plugin.getExpansionManager().getRegisteredExpansion(params.get(0));
		if (expansion == null)
		{
			Msg.msg(sender,
					"&cThere is no expansion loaded with the identifier: &f" + params.get(0));
			return;
		}


		final String message = !PlaceholderAPI.unregisterExpansion(expansion) ?
							   "&cFailed to unregister expansion: &f" :
							   "&aSuccessfully unregistered expansion: &f";

		Msg.msg(sender, message + expansion.getName());
	}

	@Override
	public void complete(@NotNull final PlaceholderAPIPlugin plugin, @NotNull final CommandSender sender, @NotNull final String alias, @NotNull @Unmodifiable final List<String> params, @NotNull final List<String> suggestions)
	{
		if (params.size() > 1)
		{
			return;
		}

		suggestByParameter(PlaceholderAPI.getRegisteredIdentifiers().stream(), suggestions, params.isEmpty() ? null : params.get(0));
	}

}
