package io.github.dwcarrot.kitpackmgr.events;

import io.github.dwcarrot.kitpackmgr.commands.InventoryUtils;
import io.github.dwcarrot.kitpackmgr.nms.NativeItemStack;
import io.github.dwcarrot.kitpackmgr.nms.NativeMarkedItemStack;
import io.github.dwcarrot.kitpackmgr.storage.IAsyncDatabase;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.util.*;


public class RightClickHandler implements Listener {

    public static String PlaceholderPlayerName = "%player_name%";

    public static long ExecuteDelay = 5;

    private final Plugin plugin;

    private final IAsyncDatabase<UUID, KitPack> db;

    private final Set<Player> onExecuting = new HashSet<>();

    public RightClickHandler(Plugin plugin, IAsyncDatabase<UUID, KitPack> db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if(!this.onExecuting.contains(player)) {
            if (event.getAction().isRightClick()) {
                PlayerInventory playerInventory = player.getInventory();
                ItemStack itemStack = playerInventory.getItemInMainHand();
                NativeItemStack w = NativeMarkedItemStack.fromItemStack(itemStack);
                if (w instanceof NativeMarkedItemStack) {
                    NativeMarkedItemStack w0 = (NativeMarkedItemStack) w;
                    final KitPack result = this.db.tryRetrieve(w0.uuid);
                    if (result == null) {
                        this.plugin.getSLF4JLogger().warn("try retrieve failed: [%s]", w0.uuid);
                        return;
                    }
                    if (result == KitPack.EMPTY) {
                        return;
                    }
                    if (result.getSelector().test(player)) {
                        int amount = itemStack.getAmount();
                        if (amount > 1) {
                            itemStack.setAmount(amount - 1);
                            playerInventory.setItemInMainHand(itemStack);
                        } else {
                            playerInventory.setItemInMainHand(null);
                        }
                        final RightClickHandler self = this;
                        this.onExecuting.add(player);
                        this.plugin.getServer()
                                .getScheduler()
                                .runTaskLater(this.plugin, () -> self.execute(result, player), ExecuteDelay);
                    }
                }
            }
        }
    }

    public void execute(KitPack kitPack, Player player) {
        if(player.isOnline()) {
            List<NativeItemStack> items = kitPack.getItems();
            ItemStack[] itemStacks = new ItemStack[items.size()];
            Iterator<NativeItemStack> iterItems = items.iterator();
            for (int i = 0; i < itemStacks.length; ++i) {
                itemStacks[i] = iterItems.next().unwrap();
            }
            Server server = player.getServer();
            ConsoleCommandSender commandSender = server.getConsoleSender();
            InventoryUtils.givePlayerItems(player, itemStacks);
            Logger logger = this.plugin.getSLF4JLogger();
            for (String command : kitPack.getCommands()) {
                final String finalCommand = command.replace(PlaceholderPlayerName, player.getName());
                if (!server.dispatchCommand(commandSender, finalCommand)) {
                    logger.warn("command execute fail: /{}", finalCommand);
                }
            }
        }
        this.onExecuting.remove(player);
    }
}
