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

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class KitItemsFunc extends ARL2ndSubCommand {

    public static final String SUBCMD = "items";

    private final Plugin plugin;

    private final Operation db;

    public KitItemsFunc(Plugin plugin, Operation db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    protected boolean invokeCommandAdd(CommandSender commandSender, Command command, String label, String[] args, int argsOffset) {
        Player player = (Player) commandSender;
        int index = -1;
        if(args.length > argsOffset) {
            try {
                index = Integer.parseInt(args[argsOffset++]);
            } catch (NumberFormatException e) {

            }
        }
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
        itemStack = InventoryUtils.getOffHandItem(player);
        final NativeItemStack itemToAdd = NativeMarkedItemStack.fromItemStack(itemStack);
        final UUID uuid = ((NativeMarkedItemStack)w).uuid;
        final int finalIndex = index;
        this.db.modify(
                uuid,
                kitPack -> kitPack.addItem(itemToAdd, finalIndex),
                new SetPlayerMainHand(player, amount, "kit-pack %s add item success"),
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
                kitPack -> kitPack.removeItem(index),
                new SetPlayerMainHand(player, amount, "kit-pack %s remove item success"),
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
                new ShowKitPackItems(player, uuid),
                this.plugin
            );
        return true;
    }

    @Override
    protected List<String> invokeTabCompleteAdd(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
        return null;
    }

    @Override
    protected List<String> invokeTabCompleteRemove(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
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

class ShowKitPackItems implements BiConsumer<KitPack, Plugin> {

    private final Player player;
    private final UUID uuid;
    private final String formatFail = "unable to find kit-pack [%s]";

    ShowKitPackItems(Player player, UUID uuid) {
        this.player = player;
        this.uuid = uuid;
    }

    @Override
    public void accept(KitPack kitPack, Plugin plugin) {
        if(kitPack != null) {
            int index = 0;
            for(NativeItemStack w : kitPack.getItems()) {
                Component head = Component.text(String.format("[%d]  ", index++));
                Component item = InventoryUtils.showItem(w.unwrap());
                this.player.sendMessage(head.append(item));
            }
        } else {
            player.sendMessage(String.format(this.formatFail, this.uuid));
        }
    }
}