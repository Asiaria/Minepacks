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

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Message.MessageClickEvent;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import at.pcgamingfreaks.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

public class RestoreCommand extends MinepacksCommand
{
	private final Message messageBackupsHeader, messageBackupsFooter, messageBackupEntry, messageUnableToLoadBackup, messageNoUserFound, messageRestored;
	private final String helpParam;
	private final SimpleDateFormat dateFormat;
	private final String[] listCommands;
	private final int elementsPerPage;

	public RestoreCommand(Minepacks plugin)
	{
		super(plugin, "restore", plugin.getLanguage().getTranslated("Commands.Description.Restore"), Permissions.RESTORE, plugin.getLanguage().getCommandAliases("Restore"));
		helpParam = "<" + plugin.getLanguage().get("Ingame.Restore.ParameterBackupName") + "> (" + plugin.getLanguage().get("Commands.PlayerNameVariable") + ")";
		messageBackupsHeader = plugin.getLanguage().getMessage("Ingame.Restore.Headline").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s");
		messageBackupsFooter = plugin.getLanguage().getMessage("Ingame.Restore.Footer").replaceAll("\\{CurrentPage}", "%1\\$d").replaceAll("\\{MaxPage}", "%2\\$d").replaceAll("\\{MainCommand}", "%3\\$s").replaceAll("\\{SubCommand}", "%4\\$s");
		messageBackupEntry = plugin.getLanguage().getMessage("Ingame.Restore.BackupEntry").replaceAll("\\{BackupIdentifier}", "%1\\$s").replaceAll("\\{BackupDate}", "%2\\$s")
				.replaceAll("\\{BackupPlayerName}", "%3\\$s").replaceAll("\\{BackupPlayerUUID}", "%4\\$s").replaceAll("\\{MainCommand}", "%5\\$s").replaceAll("\\{SubCommand}", "%6\\$s");
		messageUnableToLoadBackup = plugin.getLanguage().getMessage("Ingame.Restore.NoValidBackup").replaceAll("\\{BackupIdentifier}", "%1\\$s");
		messageNoUserFound = plugin.getLanguage().getMessage("Ingame.Restore.NoUserToRestoreToFound");
		messageRestored = plugin.getLanguage().getMessage("Ingame.Restore.Restored");
		listCommands = plugin.getLanguage().getCommandAliases("ListBackups", "list");
		//noinspection ConstantConditions
		elementsPerPage = plugin.getLanguage().getYaml().getInt("Ingame.Restore.BackupsPerPage", 10);
		dateFormat = (plugin.getLanguage().get("Ingame.Restore.BackupEntry").contains("{BackupDate}")) ? new SimpleDateFormat(plugin.getLanguage().get("Ingame.Restore.DateFormat")) : null;
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if(args.length == 1 || args.length == 2)
		{
			if(StringUtils.arrayContainsIgnoreCase(listCommands, args[0]))
			{
				listBackups(sender, mainCommandAlias, alias, args);
			}
			else
			{
				restore(sender, args);
			}
		}
		else
		{
			showHelp(sender, mainCommandAlias);
		}
	}

	@SuppressWarnings("deprecation")
	private void restore(final @NotNull CommandSender sender, final @NotNull String[] args)
	{
		ItemStack[] items = ((Minepacks) getMinepacksPlugin()).getDatabase().loadBackup(args[0]);
		if(items != null)
		{
			OfflinePlayer target = null;
			if(args.length == 2)
			{
				target = plugin.getServer().getOfflinePlayer(args[1]);
			}
			else
			{
				String[] components = args[0].split("_");
				if(components.length == 2)
				{
					target = plugin.getServer().getOfflinePlayer(components[0]);
				}
				else if(components.length == 3)
				{
					if(!components[1].contains("-"))
					{
						components[1] = components[1].replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
					}
					target = plugin.getServer().getOfflinePlayer(UUID.fromString(components[1]));
				}
			}
			if(target == null)
			{
				messageNoUserFound.send(sender);
				return;
			}
			getMinepacksPlugin().getBackpack(target, new Callback<Backpack>() {
				@Override
				public void onResult(Backpack backpack)
				{
					backpack.getInventory().setContents(items);
					backpack.setChanged();
					messageRestored.send(sender);
				}

				@Override
				public void onFail() {}
			});
		}
		else
		{
			messageUnableToLoadBackup.send(sender, args[0]);
		}
	}

	private void listBackups(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		int page = 0;
		if(args.length == 2)
		{
			try
			{
				page = StringUtils.parsePageNumber(args[1]);
			}
			catch(NumberFormatException ignored)
			{
				((Minepacks) getMinepacksPlugin()).messageNotANumber.send(sender);
				return;
			}
		}
		ArrayList<String> backups = ((Minepacks) getMinepacksPlugin()).getDatabase().getBackups();
		int pages = backups.size() / elementsPerPage + 1;
		page = Math.min(page, pages - 1);
		int offset = page * elementsPerPage, end = Math.min(offset + elementsPerPage, backups.size());
		messageBackupsHeader.send(sender, page + 1, pages, mainCommandAlias, alias + ' ' + args[0]);
		while(offset < end)
		{
			String backup = backups.get(offset++), uuid = "No UUID", date = "Unknown";
			String[] components = backup.split("_");
			if(components.length == 3) uuid = (components[1].contains("-")) ? components[1] : components[1].replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
			if(dateFormat != null && (components.length == 2 || components.length == 3))
			{
				try
				{
					date = dateFormat.format(new Date(Long.parseLong(components[components.length - 1])));
				}
				catch(NumberFormatException ignored) {}
			}
			messageBackupEntry.send(sender, backup, date, components[0], uuid, mainCommandAlias, alias);
		}
		messageBackupsFooter.send(sender, page + 1, pages, mainCommandAlias, alias + ' ' + args[0]);
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		final String arg = args[args.length - 1].toLowerCase(Locale.ROOT);
		List<String> autoComplete = null;
		if(args.length == 1)
		{
			List<String> backups = ((Minepacks) getMinepacksPlugin()).getDatabase().getBackups();
			autoComplete = new ArrayList<>();
			for(String backupId : backups)
			{
				if(backupId.toLowerCase(Locale.ROOT).startsWith(arg)) autoComplete.add(backupId);
			}
			for(String listCommand : listCommands)
			{
				if(listCommand.startsWith(arg)) autoComplete.add(listCommand);
			}
		}
		else if(args.length == 2)
		{
			autoComplete = new ArrayList<>();
			for(Player player : Bukkit.getOnlinePlayers())
			{
				if(player.getName().toLowerCase(Locale.ROOT).startsWith(arg)) autoComplete.add(player.getName());
			}
		}
		return autoComplete;
	}

	@Override
	public List<HelpData> getHelp(final @NotNull CommandSender requester)
	{
		List<HelpData> help = new ArrayList<>();
		help.add(new HelpData(getTranslatedName() + " " + listCommands[0], null, ((Minepacks) getMinepacksPlugin()).getLanguage().getTranslated("Commands.Description.RestoreList"), MessageClickEvent.ClickEventAction.RUN_COMMAND));
		help.add(new HelpData(getTranslatedName(), helpParam, getDescription(), MessageClickEvent.ClickEventAction.SUGGEST_COMMAND));
		return help;
	}
}