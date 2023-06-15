/*
 *   Copyright (C) 2020 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Command;

import at.pcgamingfreaks.Bukkit.Command.RegisterablePluginCommand;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Events.InventoryClearEvent;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Events.InventoryClearedEvent;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InventoryClearCommand implements CommandExecutor, TabCompleter
{
	private final Minepacks plugin;
	private final RegisterablePluginCommand command;
	private final Message messageUnknownPlayer, messageOwnInventoryCleared, messageOtherInventoryCleared, messageInventoryWasCleared;

	public InventoryClearCommand(final @NotNull Minepacks plugin)
	{
		this.plugin = plugin;
		command = new RegisterablePluginCommand(plugin, "clearinventory", plugin.getLanguage().getCommandAliases("InventoryClear"));
		command.registerCommand();
		command.setExecutor(this);
		command.setTabCompleter(this);

		// Load messages
		messageUnknownPlayer = plugin.getLanguage().getMessage("Ingame.InventoryClear.UnknownPlayer").replaceAll("\\{Name}", "%s");
		messageOwnInventoryCleared = plugin.getLanguage().getMessage("Ingame.InventoryClear.Cleared");
		messageOtherInventoryCleared = plugin.getLanguage().getMessage("Ingame.InventoryClear.ClearedOther").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s");
		messageInventoryWasCleared = plugin.getLanguage().getMessage("Ingame.InventoryClear.ClearedOtherTarget").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s");
	}

	public void close()
	{
		command.unregisterCommand();
	}

	private void clearInventory(Player player, CommandSender sender)
	{
		InventoryClearEvent clearEvent = new InventoryClearEvent(player, sender);
		Bukkit.getPluginManager().callEvent(clearEvent);
		if(clearEvent.isCancelled()) return;
		player.getInventory().clear();
		if(sender.equals(player))
		{
			messageOwnInventoryCleared.send(player);
		}
		else
		{
			messageInventoryWasCleared.send(player, sender.getName(), (sender instanceof Player) ? ((Player) sender).getDisplayName() : ChatColor.GRAY + sender.getName());
			messageOtherInventoryCleared.send(sender, player.getName(), player.getDisplayName());
		}
		Bukkit.getPluginManager().callEvent(new InventoryClearedEvent(player, sender));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
	{
		if(sender.hasPermission(Permissions.INVENTORY_CLEAR))
		{
			if(args.length > 0)
			{
				if(sender.hasPermission(Permissions.INVENTORY_CLEAR_OTHER))
				{
					for(String name : args)
					{
						Player player = plugin.getServer().getPlayer(name);
						if(player == null)
						{
							messageUnknownPlayer.send(sender, name);
						}
						else
						{
							clearInventory(player, sender);
						}
					}
				}
				else
				{
					plugin.messageNoPermission.send(sender);
				}
			}
			else if(sender instanceof Player)
			{
				clearInventory((Player) sender, sender);
			}
			else
			{
				sender.sendMessage("/clear <player_name>"); //TODO
			}
		}
		else
		{
			plugin.messageNoPermission.send(sender);
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length > 0 && (!(sender instanceof Player) || sender.hasPermission(Permissions.INVENTORY_CLEAR_OTHER)))
		{
			return Utils.getPlayerNamesStartingWith(args[args.length - 1], sender);
		}
		return null;
	}
}