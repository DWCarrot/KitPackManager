package io.github.dwcarrot.kitpackmgr.storage;

import io.github.dwcarrot.kitpackmgr.nms.KitPackConverter;
import io.github.dwcarrot.kitpackmgr.nms.NBTFile;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CachedFileDatabase implements IAsyncDatabase<UUID, KitPack> {

    final Plugin plugin;

    final File folder;

    final Map<UUID, KitPack> cache;

    final NBTFile<KitPack> nbtFile = new NBTFile<>(65535, new KitPackConverter());

    ConcurrentLinkedQueue<ITask> taskQueue;

    BukkitTask taskExecuting;

    public CachedFileDatabase(Plugin plugin, File folder) {
        this.plugin = plugin;
        this.folder = folder;
        this.cache = new HashMap<>();
        this.taskQueue = new  ConcurrentLinkedQueue<>();
        this.taskExecuting = null;
        this.folder.mkdirs();
        this.loadAll();
    }

    void loadAll() {
        final String ext = ".nbt";
        for(File f : this.folder.listFiles()) {
            if(f.isFile()) {
                String name = f.getName();
                if(name.endsWith(ext)) {
                    try {
                        UUID uuid = UUID.fromString(name.substring(0, name.length() - ext.length()));
                        loadFile(uuid, f);
                    } catch (IllegalArgumentException e) {
                        //skip
                    } catch (Exception e) {
                        this.plugin.getSLF4JLogger().warn("invalid file", e);
                    }
                }
            }
        }
    }

    void loadFile(UUID uuid, File file) throws Exception {
        FileInputStream ifile = new FileInputStream(file);
        KitPack kitPack = this.nbtFile.read(ifile);
        ifile.close();
        this.cache.put(uuid, kitPack);
    }

    void saveFile(UUID uuid, KitPack data) throws Exception {
        FileOutputStream ofile = new FileOutputStream(new File(this.folder, uuid.toString() + ".nbt"));
        this.nbtFile.write(ofile, data);
        ofile.close();
    }

    void deleteFile(UUID uuid) throws Exception {

    }

    void executeNext() {
        ITask task = this.taskQueue.poll();
        if (task != null) {
            if (task.isAsync()) {
                this.taskExecuting = this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, task);
            } else {
                this.taskExecuting = this.plugin.getServer().getScheduler().runTask(this.plugin, task);
            }
        } else {
            this.taskExecuting = null;
        }
    }

    void execute(ITask task, boolean linked) {
        if(linked) {
            if (task.isAsync()) {
                this.taskExecuting = this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, task);
            } else {
                this.taskExecuting = this.plugin.getServer().getScheduler().runTask(this.plugin, task);
            }
        } else {
            if(this.taskQueue.offer(task)) {
                if(this.taskExecuting == null) {
                    this.executeNext();
                }
            } else {
                task.immediatelyFail(new Exception("fail to add task"));
            }
        }
    }

    BukkitTask runSync(Runnable r) {
        return this.plugin.getServer().getScheduler().runTask(this.plugin, r);
    }

    @Override
    public <A> void create(UUID condition, KitPack data, CompletionHandler<KitPack, A> callback, A attachment, boolean linked) {
        ITask task = new CUDTaskAsync<>(condition, data, callback, attachment, this);
        this.execute(task, linked);
    }

    @Override
    public <A> void retrieve(UUID condition, CompletionHandler<KitPack, A> callback, A attachment, boolean linked) {
        ITask task = new RTaskSync<>(condition, callback, attachment, this);
        this.execute(task, linked);
    }

    @Override
    public <A> void update(UUID condition, KitPack data, CompletionHandler<KitPack, A> callback, A attachment, boolean linked) {
        ITask task = new CUDTaskAsync<>(condition, data, callback, attachment, this);
        this.execute(task, linked);
    }

    @Override
    public <A> void delete(UUID condition, CompletionHandler<KitPack, A> callback, A attachment, boolean linked) {
        ITask task = new CUDTaskAsync<>(condition, null, callback, attachment, this);
        this.execute(task, linked);
    }

    @Override
    public KitPack tryRetrieve(UUID condition) {
        // unsafe ?
        if(this.taskExecuting == null) {
            return this.cache.getOrDefault(condition, KitPack.EMPTY);
        }
        return null;
    }

    public <A>void reloadAll(CompletionHandler<Set<UUID>, A> callback, A attachment) {
        ITask task = new ReloadTaskAsync<>(callback, attachment, this);
        this.execute(task, false);
    }

}

interface ITask extends Runnable {

    boolean isAsync();

    void immediatelyFail(Exception e);
}

class CUDTaskAsync<A> implements ITask {

    private final UUID uuid;
    private final KitPack data; // null if is delete
    private final CompletionHandler<KitPack, A> callback;
    private final A attachment;
    private final CachedFileDatabase db;

    public CUDTaskAsync(UUID uuid, KitPack data, CompletionHandler<KitPack, A> callback, A attachment, CachedFileDatabase db) {
        this.uuid = uuid;
        this.data = data;
        this.callback = callback;
        this.attachment = attachment;
        this.db = db;
    }

    @Override
    public void run() {
        if(this.data != null) {
            // Create or Update
            this.db.cache.put(this.uuid, this.data);
            try {
                this.db.saveFile(uuid, this.data);
                this.db.taskExecuting = this.db.runSync(new CUDTaskSync<>(this.data, this.callback, this.attachment, this.db));
            } catch (Exception e) {
                this.db.taskExecuting = this.db.runSync(new ErrorHandleTask<>(e, this.callback, this.attachment, this.db));
            }
        } else {
            KitPack data = this.db.cache.remove(this.uuid);
            if(data != null) {
                try {
                    this.db.deleteFile(this.uuid);
                    this.db.taskExecuting = this.db.runSync(new CUDTaskSync<>(data, this.callback, this.attachment, this.db));
                } catch (Exception e) {
                    this.db.taskExecuting = this.db.runSync(new ErrorHandleTask<>(e, this.callback, this.attachment, this.db));
                }
            } else {
                this.db.taskExecuting = this.db.runSync(new CUDTaskSync<>(KitPack.EMPTY, this.callback, this.attachment, this.db));
            }
        }

    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void immediatelyFail(Exception e) {
        this.callback.failed(e, this.attachment);
    }
}

class CUDTaskSync<A> implements ITask {

    private final KitPack data;
    private final CompletionHandler<KitPack, A> callback;
    private final A attachment;
    private final CachedFileDatabase db;

    public CUDTaskSync(KitPack data, CompletionHandler<KitPack, A> callback, A attachment, CachedFileDatabase db) {
        this.data = data;
        this.callback = callback;
        this.attachment = attachment;
        this.db = db;
    }

    @Override
    public void run() {
        this.callback.completed(this.data, this.attachment);
        this.db.executeNext();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void immediatelyFail(Exception e) {
        this.callback.failed(e, this.attachment);
    }
}

class RTaskSync<A> implements ITask {

    private final UUID uuid;
    private final CompletionHandler<KitPack, A> callback;
    private final A attachment;
    private final CachedFileDatabase db;

    public RTaskSync(UUID uuid, CompletionHandler<KitPack, A> callback, A attachment, CachedFileDatabase db) {
        this.uuid = uuid;
        this.callback = callback;
        this.attachment = attachment;
        this.db = db;
    }

    @Override
    public void run() {
        KitPack data;
        if(this.uuid != KitPackAll.QUERY) {
            data = this.db.cache.getOrDefault(this.uuid, KitPack.EMPTY);
        } else {
            data = new KitPackAll(this.db.cache.values());
        }
        this.callback.completed(data, this.attachment);
        this.db.executeNext();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void immediatelyFail(Exception e) {
        this.callback.failed(e, this.attachment);
    }
}


class ErrorHandleTask<A> implements ITask {

    private final Exception e;
    private final CompletionHandler<KitPack, A> callback;
    private final A attachment;
    private final CachedFileDatabase db;

    public ErrorHandleTask(Exception e, CompletionHandler<KitPack, A> callback, A attachment, CachedFileDatabase db) {
        this.e = e;
        this.callback = callback;
        this.attachment = attachment;
        this.db = db;
    }

    @Override
    public void run() {
        this.callback.failed(this.e, this.attachment);
        this.db.executeNext();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void immediatelyFail(Exception e) {
        this.callback.failed(this.e, this.attachment);
    }
}

class ReloadTaskAsync<A> implements ITask {

    private final CompletionHandler<Set<UUID>, A> callback;
    private final A attachment;
    private final CachedFileDatabase db;

    ReloadTaskAsync(CompletionHandler<Set<UUID>, A> callback, A attachment, CachedFileDatabase db) {
        this.callback = callback;
        this.attachment = attachment;
        this.db = db;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void immediatelyFail(Exception e) {
        this.callback.failed(e, this.attachment);
    }

    @Override
    public void run() {
        this.db.cache.clear();
        this.db.loadAll();
        this.db.taskExecuting = this.db.runSync(new ReloadTaskSync<>(this.callback, this.attachment, this.db));
    }
}

class ReloadTaskSync<A> implements ITask {

    private final CompletionHandler<Set<UUID>, A> callback;
    private final A attachment;
    private final CachedFileDatabase db;

    ReloadTaskSync(CompletionHandler<Set<UUID>, A> callback, A attachment, CachedFileDatabase db) {
        this.callback = callback;
        this.attachment = attachment;
        this.db = db;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void immediatelyFail(Exception e) {
        this.callback.failed(e, this.attachment);
    }

    @Override
    public void run() {
        this.callback.completed(this.db.cache.keySet(), this.attachment);
        this.db.executeNext();
    }
}