package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.controls.Operation;
import io.github.dwcarrot.kitpackmgr.storage.CachedFileDatabase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {

    private Map<String, ISubCommand> subCommands = new HashMap<>();

    public Commands(Plugin plugin, CachedFileDatabase db) {
        Operation dbOperation = new Operation(db);
        this.subCommands.put(SetItemFunc.SUBCMD, new SetItemFunc(plugin, dbOperation));
        this.subCommands.put(OfferItemFunc.SUBCMD, new OfferItemFunc(plugin, dbOperation));
        this.subCommands.put(KitItemsFunc.SUBCMD, new KitItemsFunc(plugin, dbOperation));
        this.subCommands.put(KitCommandsFunc.SUBCMD, new KitCommandsFunc(plugin, dbOperation));
        this.subCommands.put(SelectorFunc.SUBCMD, new SelectorFunc(plugin, dbOperation));
        this.subCommands.put(ListFunc.SUBCMD, new ListFunc(plugin, dbOperation));
        this.subCommands.put(ReloadFunc.SUBCMD, new ReloadFunc(plugin, db));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length > 0) {
            ISubCommand subCommand = this.subCommands.get(args[0]);
            if(subCommand != null) {
                if(subCommand.checkPermission(sender)) {
                    if(!subCommand.invokeCommand(sender, command, label, args, 1)) {
                        sender.sendMessage("fail", subCommand.getHelp('/' + label));
                    }
                } else {
                    sender.sendMessage("permission denied");
                }
            } else {
                sender.sendMessage("invalid sub-command: " + args[0]);
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0 || "".equals(args[0])) {
            return this.subCommands.entrySet()
                    .stream()
                    .filter((Map.Entry<String, ISubCommand> e) -> e.getValue().checkPermission(sender))
                    .map((Map.Entry<String, ISubCommand> e) -> e.getKey() )
                    .collect(Collectors.toUnmodifiableList());
        }
        ISubCommand subCommand = this.subCommands.get(args[0]);
        if(subCommand != null) {
            if(subCommand.checkPermission(sender)) {
                return subCommand.invokeTabComplete(sender, command, label, args, 1);
            }
        }
        return null;
    }

}
