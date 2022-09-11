package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.controls.Operation;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class OfferItemFunc implements ISubCommand {

    public static final String SUBCMD = "offer";

    private final Plugin plugin;

    private final Operation db;

    public OfferItemFunc(Plugin plugin, Operation db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args, int argsOffset) {
        if(args.length <= argsOffset) {
            return false;
        }
        Server server = commandSender.getServer();
        String maybePlayer = args[argsOffset++];
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
            kitUUID = UUID.fromString(args[argsOffset++]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        int count = 1;
        if (args.length > 3) {
            try {
                count =  Integer.parseInt(args[argsOffset++]);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        this.db.retrieve(kitUUID, new SetPlayerInventory(player, count), this.plugin);
        return true;
    }

    @Override
    public List<String> invokeTabComplete(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
        if(args.length > argsOffset) {
            if(args.length > argsOffset + 1) {
                if(args.length > argsOffset + 2) {
                    if(args[argsOffset+2].isEmpty()) {
                        return List.of("?<amount>");
                    }
                }
                if(args[argsOffset+1].isEmpty()) {
                    return List.of("<uuid>");
                }
            } else {
                Server server = sender.getServer();
                final String prefix = args[argsOffset];
                return server.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .filter(name -> name.startsWith(prefix))
                        .toList();
            }
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


class SetPlayerInventory implements BiConsumer<KitPack, Plugin> {

    private final Player player;
    private final int amount;

    SetPlayerInventory(Player player, int amount) {
        this.player = player;
        this.amount = amount;
    }

    @Override
    public void accept(KitPack kitPack, Plugin plugin) {
        if(kitPack != null) {
            ItemStack itemStack = kitPack.getBind().unwrap();
            itemStack.setAmount(this.amount);
            InventoryUtils.givePlayerItems(this.player, itemStack);
        }
    }
}