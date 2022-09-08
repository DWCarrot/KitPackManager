package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.controls.Operation;
import io.github.dwcarrot.kitpackmgr.nms.NativeItemStack;
import io.github.dwcarrot.kitpackmgr.nms.NativeMarkedItemStack;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public class SetItemFunc implements ISubCommand {

    public static final String SUBCMD = "set";

    private final Plugin plugin;

    private final Operation db;

    public SetItemFunc(Plugin plugin, Operation db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = (Player) commandSender;
        PlayerInventory inventory = player.getInventory();
        ItemStack itemStack = inventory.getItemInMainHand();
        if(itemStack.getType() == Material.AIR) {
            player.sendMessage("nothing in main-hand");
            return true;
        }
        if(itemStack.getAmount() == 1) {
            NativeItemStack w = NativeMarkedItemStack.fromItemStack(itemStack);
            if(w instanceof NativeMarkedItemStack) {
                this.db.update(
                        new KitPack((NativeMarkedItemStack)w),
                        player,
                        PlayerInventory::setItemInMainHand,
                        this.plugin
                    );
            } else {
                final UUID uuid = UUID.randomUUID();
                this.db.create(
                        new KitPack(w.set(uuid)),
                        player,
                        PlayerInventory::setItemInMainHand,
                        this.plugin
                    );
            }
        } else {
            player.sendMessage("too many items in main hand");
        }
        return true;
    }

    @Override
    public List<String> invokeTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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
