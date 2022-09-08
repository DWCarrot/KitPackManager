package io.github.dwcarrot.kitpackmgr.events;

import io.github.dwcarrot.kitpackmgr.nms.NativeItemStack;
import io.github.dwcarrot.kitpackmgr.nms.NativeMarkedItemStack;
import io.github.dwcarrot.kitpackmgr.storage.IAsyncDatabase;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class RightClickHandler implements Listener {

    private final Plugin plugin;

    private final IAsyncDatabase<UUID, KitPack> db;

    public RightClickHandler(Plugin plugin, IAsyncDatabase<UUID, KitPack> db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getAction().isRightClick()) {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            NativeItemStack w = NativeMarkedItemStack.fromItemStack(itemStack);
            if(w instanceof NativeMarkedItemStack) {
                NativeMarkedItemStack w0 = (NativeMarkedItemStack) w;
                KitPack result = this.db.tryRetrieve(w0.uuid);
                if(result == null) {
                    this.plugin.getSLF4JLogger().warn("try retrieve failed: [%s]", w0.uuid);
                    return;
                }
                if(result == KitPack.EMPTY) {
                    return;
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                player.sendMessage(itemMeta.displayName().append(Component.text(((NativeMarkedItemStack)w).uuid.toString())));
            }
        }
    }
}
