package io.github.dwcarrot.kitpackmgr.events;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RightClickHandler implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getAction().isRightClick()) {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            ItemMeta itemMeta = itemStack.getItemMeta();
            player.sendMessage(itemMeta.displayName());
//            YamlConfiguration cfg = new YamlConfiguration();
//            cfg.getItemStack("1");
//            cfg.set("1", itemStack);
        }
    }
}
