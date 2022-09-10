package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.controls.Operation;
import io.github.dwcarrot.kitpackmgr.nms.NativeItemStack;
import io.github.dwcarrot.kitpackmgr.nms.NativeMarkedItemStack;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SetItemFunc implements ISubCommand {

    public static final String SUBCMD = "set";

    private final Plugin plugin;

    private final Operation db;

    public SetItemFunc(Plugin plugin, Operation db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args, int argsOffset) {
        Player player = (Player) commandSender;
        ItemStack itemStack = InventoryUtils.getMainHandItem(player);
        if(itemStack == null) {
            return true;
        }
        int amount = itemStack.getAmount();
        NativeItemStack w = NativeMarkedItemStack.fromItemStack(itemStack);
        if (w instanceof NativeMarkedItemStack) {
            final NativeMarkedItemStack w0 = (NativeMarkedItemStack)w;
            this.db.modify(
                    w0.uuid,
                    kitPack -> kitPack.setBind(w0),
                    new SetPlayerMainHand(player, amount, "kit-pack created: %s"),
                    this.plugin
                );
        } else {
            final UUID uuid = UUID.randomUUID();
            this.db.create(
                    new KitPack(w.set(uuid)),
                    new SetPlayerMainHand(player, amount, "kit-pack created: %s"),
                    this.plugin
                );
        }
        return true;
    }

    @Override
    public List<String> invokeTabComplete(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
        return null;
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        if(commandSender instanceof Player && commandSender.hasPermission("kitpackmanager.op")) {
            return true;
        }
        return false;
    }

    @Override
    public String getHelp(String main) {
        return main + ' ' + SetItemFunc.SUBCMD;
    }
}

class SetPlayerMainHand implements BiConsumer<KitPack, Plugin> {

    private final Player player;
    private final int amount;
    private final String formatSuccess;

    SetPlayerMainHand(Player player, int amount, String formatSuccess) {
        this.player = player;
        this.amount = amount;
        this.formatSuccess = formatSuccess;
    }

    @Override
    public void accept(KitPack kitPack, Plugin plugin) {
        if(kitPack != null) {
            PlayerInventory playerInventory = this.player.getInventory();
            ItemStack itemStack = kitPack.getBind().unwrap(this.amount);
            playerInventory.setItemInMainHand(itemStack);
            this.player.sendMessage(String.format(this.formatSuccess, kitPack.getBind().uuid));
        }
    }
}