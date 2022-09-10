package io.github.dwcarrot.kitpackmgr.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class InventoryUtils {

    public static void givePlayerItems(Player player, ItemStack ... items) {
        PlayerInventory playerInventory = player.getInventory();
        Map<Integer, ItemStack> rest = playerInventory.addItem(items);
        if(rest.size() > 0) {
            player.sendMessage("your inventory is full. item dropped");
            World world = player.getWorld();
            Location location = player.getLocation();
            location.add(0.5, 1, 0.5);
            for(ItemStack itemStack : rest.values()) {
                world.dropItem(location, itemStack);
            }
        }
    }

    public static ItemStack getMainHandItem(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack itemStack = playerInventory.getItemInMainHand();
        if(itemStack.getType() == Material.AIR) {
            player.sendMessage("nothing in main-hand");
            return null;
        }
//        if(itemStack.getAmount() != 1) {
//            player.sendMessage("too many items in main hand");
//            return null;
//        }
        return itemStack;
    }

    public static ItemStack getOffHandItem(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack itemStack = playerInventory.getItemInOffHand();
        if(itemStack.getType() == Material.AIR) {
            player.sendMessage("nothing in off-hand");
            return null;
        }
        return itemStack;
    }

    public static Component showItem(ItemStack itemStack) {
        Component displayName = itemStack.displayName();
        Component amount = Component.text("x " + itemStack.getAmount());
        return displayName.append(amount);
    }

    public static Component showCommands(String command, int limit) {
        Component text;
        TextColor color = TextColor.color(0x55, 0xFF, 0x55);
        if(command.length() < limit) {
            text = Component.text(command, color);
        } else {
            text = Component.text(command.substring(0, limit), color).append(Component.text("..."));
        }
        return Component.text(command);
    }
}
