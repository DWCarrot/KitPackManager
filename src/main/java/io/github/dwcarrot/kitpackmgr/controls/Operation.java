package io.github.dwcarrot.kitpackmgr.controls;

import io.github.dwcarrot.kitpackmgr.storage.IAsyncDatabase;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import io.github.dwcarrot.kitpackmgr.storage.KitPackAll;
import org.bukkit.plugin.Plugin;

import java.nio.channels.CompletionHandler;
import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Operation {

    private final IAsyncDatabase<UUID, KitPack> db;

    public Operation(IAsyncDatabase<UUID, KitPack> db) {
        this.db = db;
    }

    public void create(KitPack kitPack, BiConsumer<KitPack, Plugin> callback, Plugin plugin) {
        this.db.create(
                kitPack.getBind().uuid,
                kitPack,
                new SingleCompletionHandler(callback, "database create error"),
                plugin,
                false
            );
    }

    public void update(KitPack kitPack, BiConsumer<KitPack, Plugin> callback, Plugin plugin) {
        this.db.update(
                kitPack.getBind().uuid,
                kitPack,
                new SingleCompletionHandler(callback, "database update error"),
                plugin,
                false
            );
    }

    public void retrieve(UUID uuid, BiConsumer<KitPack, Plugin> callback, Plugin plugin) {
        this.db.retrieve(
                uuid,
                new SingleCompletionHandler(callback, "database retrieve error"),
                plugin,
                false
            );
    }

    public void delete(UUID uuid, BiConsumer<KitPack, Plugin> callback, Plugin plugin) {
        this.db.delete(uuid,
                new SingleCompletionHandler(callback, "database retrieve error"),
                plugin,
                false
            );
    }

    public void modify(UUID uuid, Predicate<KitPack> modifier, BiConsumer<KitPack, Plugin> callback, Plugin plugin) {
        this.db.retrieve(
                uuid,
                new LinkedCompletionHandler(modifier, callback, this.db),
                plugin,
                false
            );
    }

    public void list(BiConsumer<Collection<KitPack>, Plugin> callback, Plugin plugin) {
        this.db.retrieve(
                KitPackAll.QUERY,
                new ListCompletionHandler(callback),
                plugin,
                false
            );
    }
}

class SingleCompletionHandler implements CompletionHandler<KitPack, Plugin> {

    private final BiConsumer<KitPack, Plugin> callback;
    private final String error;

    SingleCompletionHandler(BiConsumer<KitPack, Plugin> callback, String error) {
        this.callback = callback;
        this.error = error;
    }

    @Override
    public void completed(KitPack result, Plugin attachment) {
        this.callback.accept(result == KitPack.EMPTY ? null : result, attachment);
    }

    @Override
    public void failed(Throwable exc, Plugin attachment) {
        attachment.getSLF4JLogger().error(this.error, exc);
    }
}

class LinkedCompletionHandler implements CompletionHandler<KitPack, Plugin> {

    private final Predicate<KitPack> modifier;
    private final BiConsumer<KitPack, Plugin> callback;
    private final IAsyncDatabase<UUID, KitPack> db;

    LinkedCompletionHandler(Predicate<KitPack> modifier, BiConsumer<KitPack, Plugin> callback, IAsyncDatabase<UUID, KitPack> db) {
        this.modifier = modifier;
        this.callback = callback;
        this.db = db;
    }

    @Override
    public void completed(KitPack result, Plugin attachment) {
        final KitPack kitPack = result == KitPack.EMPTY ? null : result;
        boolean next = this.modifier.test(kitPack);
        if(kitPack != null && next) {
            this.db.update(
                    kitPack.getBind().uuid,
                    kitPack,
                    new SingleCompletionHandler(this.callback, "database update error"),
                    attachment,
                    true
                );
        }
    }

    @Override
    public void failed(Throwable exc, Plugin attachment) {
        attachment.getSLF4JLogger().error("database retrieve error", exc);
    }
}

class ListCompletionHandler implements CompletionHandler<KitPack, Plugin> {

    private final BiConsumer<Collection<KitPack>, Plugin> callback;

    ListCompletionHandler(BiConsumer<Collection<KitPack>, Plugin> callback) {
        this.callback = callback;
    }

    @Override
    public void completed(KitPack result, Plugin attachment) {
        this.callback.accept(((KitPackAll)result).kitPacks, attachment);
    }

    @Override
    public void failed(Throwable exc, Plugin attachment) {
        attachment.getSLF4JLogger().error("database retrieve error", exc);
    }
}