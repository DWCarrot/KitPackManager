package io.github.dwcarrot.kitpackmgr.controls;

import io.github.dwcarrot.kitpackmgr.storage.IAsyncDatabase;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Operation {

    private final IAsyncDatabase<UUID, KitPack> db;

    public Operation(IAsyncDatabase<UUID, KitPack> db) {
        this.db = db;
    }

    public void create(KitPack kitPack, Player commandSender, BiConsumer<PlayerInventory, ItemStack> setInventory, Plugin plugin) {
        this.db.create(kitPack.getBind().uuid, kitPack, new CreateCallback(commandSender, setInventory), plugin, false);
    }

    public void update(KitPack kitPack, Player commandSender, BiConsumer<PlayerInventory, ItemStack> setInventory, Plugin plugin) {
        this.db.update(kitPack.getBind().uuid, kitPack, new UpdateCallback(commandSender, setInventory), plugin, false);
    }

    public void offer(UUID uuid, int amount, Player target, CommandSender commandSender, Plugin plugin) {
        this.db.retrieve(uuid, new OfferCallback(commandSender, target, uuid, amount), plugin, false);
    }

    public void delete(UUID uuid, Player commandSender, Plugin plugin) {
        this.db.delete(uuid, new DeleteCallback(commandSender), plugin, false);
    }

    public void modify(UUID uuid, Predicate<KitPack> modifier, Player commandSender, BiConsumer<PlayerInventory, ItemStack> setInventory, Plugin plugin) {
        this.db.retrieve(uuid, new ModifyCallback(commandSender, modifier, this.db, setInventory), plugin, false);
    }
}

class CreateCallback implements CompletionHandler<KitPack, Plugin> {

    private final Player player;
    private final BiConsumer<PlayerInventory, ItemStack> setInventory;

    CreateCallback(Player player, BiConsumer<PlayerInventory, ItemStack> setInventory) {
        this.player = player;
        this.setInventory = setInventory;
    }

    @Override
    public void completed(KitPack result, Plugin attachment) {
        if(result != KitPack.EMPTY) {
            PlayerInventory playerInventory = this.player.getInventory();
            ItemStack itemStack = result.getBind().unwrap();
            this.setInventory.accept(playerInventory, itemStack);
            this.player.sendMessage("kit-pack create: " + result.getBind().uuid);
        } else {
            this.player.sendMessage("can not create kit-pack");
        }
    }

    @Override
    public void failed(Throwable exc, Plugin attachment) {
        attachment.getSLF4JLogger().error("database create error", exc);
    }
}

class UpdateCallback implements CompletionHandler<KitPack, Plugin> {

    private final Player player;
    private final BiConsumer<PlayerInventory, ItemStack> setInventory;
    private final String s;

    UpdateCallback(Player player, BiConsumer<PlayerInventory, ItemStack> setInventory) {
        this.player = player;
        this.setInventory = setInventory;
        this.s = "kit-pack updated: ";
    }

    UpdateCallback(Player player, BiConsumer<PlayerInventory, ItemStack> setInventory, boolean s) {
        this.player = player;
        this.setInventory = setInventory;
        this.s = "kit-pack modified: ";
    }

    @Override
    public void completed(KitPack result, Plugin attachment) {
        if(result != KitPack.EMPTY) {
            PlayerInventory playerInventory = this.player.getInventory();
            ItemStack itemStack = result.getBind().unwrap();
            this.setInventory.accept(playerInventory, itemStack);
            this.player.sendMessage(this.s + result.getBind().uuid);
        } else {
            this.player.sendMessage("can not update kit-pack");
        }
    }

    @Override
    public void failed(Throwable exc, Plugin attachment) {
        attachment.getSLF4JLogger().error("database update error", exc);
    }
}

class DeleteCallback implements CompletionHandler<KitPack, Plugin> {
    private final Player player;

    DeleteCallback(Player player) {
        this.player = player;
    }

    @Override
    public void completed(KitPack result, Plugin attachment) {
        if(result != KitPack.EMPTY) {
            ItemStack itemStack = result.getBind().unwrap();
            this.player.sendMessage("kit-pack deleted: " + result.getBind().uuid);
        } else {
            this.player.sendMessage("can not delete kit-pack");
        }
    }

    @Override
    public void failed(Throwable exc, Plugin attachment) {
        attachment.getSLF4JLogger().error("database delete error", exc);
    }
}

class OfferCallback implements CompletionHandler<KitPack, Plugin> {

    private final CommandSender commandSender;
    private final Player player;
    private final UUID query;
    private final int amount;

    OfferCallback(CommandSender commandSender, Player player, UUID query, int amount) {
        this.commandSender = commandSender;
        this.player = player;
        this.query = query;
        this.amount = amount;
    }

    @Override
    public void completed(KitPack result, Plugin attachment) {
        if(result != KitPack.EMPTY) {
            PlayerInventory inventory = this.player.getInventory();
            ItemStack itemStack = result.getBind().unwrap();
            itemStack.setAmount(this.amount);
            Map<Integer, ItemStack> over = inventory.addItem(itemStack);
            if(over.size() > 0) {
                attachment.getSLF4JLogger().info("can not offer kit-pack to <%s>: unable to store", this.player.getName());
            }
        } else {
            if(this.commandSender instanceof Player) {
                this.commandSender.sendMessage("can not find specific kit-pack");
            } else {
                attachment.getSLF4JLogger().warn("can not find specific kit-pack [%s] for %s", this.query, this.player.getName());
            }
        }
    }

    @Override
    public void failed(Throwable exc, Plugin attachment) {
        attachment.getSLF4JLogger().error("database update error", exc);
    }
}

class ModifyCallback implements CompletionHandler<KitPack, Plugin> {

    private final Player player;
    private final Predicate<KitPack> modifier; // return changed
    private final IAsyncDatabase<UUID, KitPack> db;

    private final BiConsumer<PlayerInventory, ItemStack> setInventory;

    ModifyCallback(Player player, Predicate<KitPack> modifier, IAsyncDatabase<UUID, KitPack> db, BiConsumer<PlayerInventory, ItemStack> setInventory) {
        this.player = player;
        this.modifier = modifier;
        this.db = db;
        this.setInventory = setInventory;
    }

    @Override
    public void completed(KitPack result, Plugin attachment) {
        if(result != KitPack.EMPTY) {
            boolean changed = this.modifier.test(result);
            if(changed) {
                this.db.update(result.getBind().uuid, result, new UpdateCallback(this.player, this.setInventory, true), attachment, true);
            }
        } else {
            this.player.sendMessage("can not modify kit-pack");
        }
    }

    @Override
    public void failed(Throwable exc, Plugin attachment) {

    }
}