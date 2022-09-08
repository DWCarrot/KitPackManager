package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.controls.Operation;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public class OfferItemFunc implements ISubCommand {

    public static final String SUBCMD = "offer";

    private final Plugin plugin;

    private final Operation db;

    public OfferItemFunc(Plugin plugin, Operation db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(args.length > 2) {
            Server server = commandSender.getServer();
            String maybePlayer = args[1];
            Player player;
            try {
                UUID playerUUID = UUID.fromString(maybePlayer);
                player = server.getPlayer(playerUUID);
            } catch (IllegalArgumentException e) {
                player = server.getPlayer(maybePlayer);
            }
            if(player == null || (!player.isOnline())) {
                commandSender.sendMessage("target player is not online");
            }
            UUID kitUUID;
            try {
                kitUUID = UUID.fromString(args[2]);
            } catch (IllegalArgumentException e) {
                return false;
            }
            int count = 1;
            if (args.length > 3) {
                try {
                    count =  Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            this.db.offer(kitUUID, count, player, commandSender, this.plugin);
            return true;
        }
        return false;
    }

    @Override
    public List<String> invokeTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 2) {
            Server server = sender.getServer();
            final String prefix = args[1];
            return server.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .filter((String name) -> {
                        return name.startsWith(prefix);
                    })
                    .toList();
        }
        return null;
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        if(commandSender.hasPermission("kitpackmanager.op")) {
            return true;
        }
        return false;
    }

    @Override
    public String getHelp(String main) {
        return main + ' ' + SUBCMD + ' ' + "<player>" + ' ' + "<uuid>";
    }
}
