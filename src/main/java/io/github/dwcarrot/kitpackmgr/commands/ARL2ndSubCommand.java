package io.github.dwcarrot.kitpackmgr.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class ARL2ndSubCommand implements ISubCommand {

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args, int argsOffset) {
        if(args.length <= argsOffset) {
            return false;
        }
        switch (args[argsOffset++]) {
            case "add":
                return this.invokeCommandAdd(commandSender, command, label, args, argsOffset);
            case "remove":
                return this.invokeCommandRemove(commandSender, command, label, args, argsOffset);
            case "list":
                return this.invokeCommandList(commandSender, command, label, args, argsOffset);
            default:
                return false;
        }
    }

    protected abstract boolean invokeCommandAdd(CommandSender commandSender, Command command, String label, String[] args, int argsOffset);

    protected abstract boolean invokeCommandRemove(CommandSender commandSender, Command command, String label, String[] args, int argsOffset);

    protected abstract boolean invokeCommandList(CommandSender commandSender, Command command, String label, String[] args, int argsOffset);
    @Override
    public List<String> invokeTabComplete(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
        if(args.length > argsOffset) {
            if (args[argsOffset].isEmpty()) { // Sub-Command
                return List.of("add", "remove", "list");
            }
            switch (args[argsOffset++]) {
                case "add":
                    return this.invokeTabCompleteAdd(sender, command, alias, args, argsOffset);
                case "remove":
                    return this.invokeTabCompleteRemove(sender, command, alias, args, argsOffset);
                case "list":
                    return this.invokeTabCompleteList(sender, command, alias, args, argsOffset);
                default:
                    return null;
            }
        }
        return null;
    }

    protected abstract List<String> invokeTabCompleteAdd(CommandSender sender, Command command, String alias, String[] args, int argsOffset);

    protected abstract List<String> invokeTabCompleteRemove(CommandSender sender, Command command, String alias, String[] args, int argsOffset);

    protected abstract List<String> invokeTabCompleteList(CommandSender sender, Command command, String alias, String[] args, int argsOffset);
        @Override
    public boolean checkPermission(CommandSender commandSender) {
        if(commandSender instanceof Player && commandSender.hasPermission("kitpackmanager.op")) {
            return true;
        }
        return false;
    }
}
