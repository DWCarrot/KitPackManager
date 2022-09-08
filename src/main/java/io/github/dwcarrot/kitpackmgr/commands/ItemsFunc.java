package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.controls.Operation;
import io.github.dwcarrot.kitpackmgr.nms.NativeItemStack;
import io.github.dwcarrot.kitpackmgr.nms.NativeMarkedItemStack;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ItemsFunc extends ARL2ndSubCommand {

    public static final String SUBCMD = "items";

    private final Plugin plugin;

    private final Operation db;

    public ItemsFunc(Plugin plugin, Operation db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    protected boolean invokeCommandAdd(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = (Player) commandSender;
        PlayerInventory playerInventory = player.getInventory();
        ItemStack itemStack = playerInventory.getItemInMainHand();
        if(itemStack.getType() == Material.AIR) {
            player.sendMessage("nothing in main-hand");
            return true;
        }
        if(itemStack.getAmount() > 1) {
            player.sendMessage("too many items in main hand");
            return true;
        }
        NativeItemStack w = NativeMarkedItemStack.fromItemStack(itemStack);
        if(!(w instanceof NativeMarkedItemStack)) {
            player.sendMessage("no kit-pack in main-hand");
        }
        NativeMarkedItemStack w0 = (NativeMarkedItemStack)w;

        itemStack = playerInventory.getItemInOffHand();
        if(itemStack.getType() == Material.AIR) {
            player.sendMessage("nothing in off-hand");
            return true;
        }
        NativeItemStack finalW = NativeMarkedItemStack.fromItemStack(itemStack);
        this.db.modify(
                w0.uuid,
                kitPack -> kitPack.addItem(finalW),
                player,
                PlayerInventory::setItemInMainHand,
                this.plugin
            );
        return true;
    }

    @Override
    protected boolean invokeCommandRemove(CommandSender commandSender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    protected boolean invokeCommandList(CommandSender commandSender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    protected List<String> invokeTabCompleteAdd(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    protected List<String> invokeTabCompleteRemove(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    protected List<String> invokeTabCompleteList(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public String getHelp(String main) {
        return main + ' ' + SUBCMD + ' ' + "[add|remove|list]";
    }
}
