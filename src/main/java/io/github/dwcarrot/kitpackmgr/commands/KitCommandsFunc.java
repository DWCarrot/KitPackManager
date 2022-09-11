package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.controls.Operation;
import io.github.dwcarrot.kitpackmgr.nms.NativeItemStack;
import io.github.dwcarrot.kitpackmgr.nms.NativeMarkedItemStack;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class KitCommandsFunc extends ARL2ndSubCommand {

    public static String SUBCMD = "commands";

    private final Plugin plugin;

    private final Operation db;

    public KitCommandsFunc(Plugin plugin, Operation db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    protected boolean invokeCommandAdd(CommandSender commandSender, Command command, String label, String[] args, int argsOffset) {
        Player player = (Player) commandSender;
        int index = -1;
        if(args.length <= argsOffset) {
            return false;
        }

        try {
            index = Integer.parseInt(args[argsOffset]);
            argsOffset++;
            if(args.length <= argsOffset) {
                return false;
            }
        } catch (NumberFormatException e) {

        }
        final String cmdStr = String.join(" ", Arrays.asList(args).subList(argsOffset++, args.length));
        ItemStack itemStack;
        itemStack = InventoryUtils.getMainHandItem(player);
        if (itemStack == null) {
            return true;
        }
        int amount = itemStack.getAmount();
        NativeItemStack w = NativeMarkedItemStack.fromItemStack(itemStack);
        if(!(w instanceof NativeMarkedItemStack)) {
            player.sendMessage("no kit-pack in main-hand");
        }
        final UUID uuid = ((NativeMarkedItemStack)w).uuid;
        final int finalIndex = index;
        this.db.modify(
                uuid,
                kitPack -> kitPack.addCommand(cmdStr, finalIndex),
                new SetPlayerMainHand(player, amount, "kit-pack %s add command success"),
                this.plugin
        );
        return true;
    }

    @Override
    protected boolean invokeCommandRemove(CommandSender commandSender, Command command, String label, String[] args, int argsOffset) {
        Player player = (Player) commandSender;
        if(args.length <= argsOffset) {
            return false;
        }
        int index;
        try {
            index = Integer.parseInt(args[argsOffset++]);
        } catch (NumberFormatException e) {
            return false;
        }

        ItemStack itemStack = InventoryUtils.getMainHandItem(player);
        if (itemStack == null) {
            return true;
        }
        int amount = itemStack.getAmount();
        NativeItemStack w = NativeMarkedItemStack.fromItemStack(itemStack);
        if(!(w instanceof NativeMarkedItemStack)) {
            player.sendMessage("no kit-pack in main-hand");
        }
        final UUID uuid = ((NativeMarkedItemStack)w).uuid;
        this.db.modify(
                uuid,
                kitPack -> kitPack.removeCommand(index),
                new SetPlayerMainHand(player, amount, "kit-pack %s remove command success"),
                this.plugin
        );
        return true;
    }

    @Override
    protected boolean invokeCommandList(CommandSender commandSender, Command command, String label, String[] args, int argsOffset) {
        Player player = (Player) commandSender;
        ItemStack itemStack;
        itemStack = InventoryUtils.getMainHandItem(player);
        if (itemStack == null) {
            return true;
        }
        NativeItemStack w = NativeMarkedItemStack.fromItemStack(itemStack);
        if(!(w instanceof NativeMarkedItemStack)) {
            player.sendMessage("no kit-pack in main-hand");
        }
        final UUID uuid = ((NativeMarkedItemStack)w).uuid;
        this.db.retrieve(
                uuid,
                new ShowKitPackCommands(player, uuid),
                this.plugin
        );
        return true;
    }

    @Override
    protected List<String> invokeTabCompleteAdd(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
        if(args.length > argsOffset) {
            if(args.length > argsOffset + 1) {
                if(args[argsOffset+1].isEmpty()) {
                    return List.of("<command>");
                }
            }
            if(args[argsOffset].isEmpty()) {
                return List.of("<command>", "?<index>");
            }
        }
        return null;
    }

    @Override
    protected List<String> invokeTabCompleteRemove(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
        if(args.length > argsOffset) {
            if(args[argsOffset].isEmpty()) {
                return List.of("<index>");
            }
        }
        return null;
    }

    @Override
    protected List<String> invokeTabCompleteList(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
        return null;
    }

    @Override
    public String getHelp(String main) {
        return main + ' ' + SUBCMD + ' ' + "[add|remove|list]";
    }
}

class ShowKitPackCommands implements BiConsumer<KitPack, Plugin> {

    private final Player player;
    private final UUID uuid;
    private final String formatFail = "unable to find kit-pack [%s]";

    ShowKitPackCommands(Player player, UUID uuid) {
        this.player = player;
        this.uuid = uuid;
    }

    @Override
    public void accept(KitPack kitPack, Plugin plugin) {
        if(kitPack != null) {
            int index = 0;
            for(String c : kitPack.getCommands()) {
                Component head = Component.text(String.format("[%d]  ", index++));
                Component item = InventoryUtils.showCommands(c, 64);
                this.player.sendMessage(head.append(item));
            }
        } else {
            player.sendMessage(String.format(this.formatFail, this.uuid));
        }
    }
}