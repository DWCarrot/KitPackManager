package io.github.dwcarrot.kitpackmgr.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ISubCommand {

    boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args, int argsOffset);

    List<String> invokeTabComplete(CommandSender sender, Command command, String alias, String[] args, int argsOffset);

    boolean checkPermission(CommandSender commandSender);

    String getHelp(String main);
}
