package io.github.dwcarrot.kitpackmgr;

import io.github.dwcarrot.kitpackmgr.commands.Commands;
import io.github.dwcarrot.kitpackmgr.events.RightClickHandler;
import io.github.dwcarrot.kitpackmgr.storage.CachedFileDatabase;
import org.bukkit.plugin.java.JavaPlugin;

public final class KitPackManager extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        CachedFileDatabase db = new CachedFileDatabase(this, this.getDataFolder());
        Commands commands = new Commands(this, db);
        this.getServer().getPluginManager().registerEvents(new RightClickHandler(this, db), this);
        this.getServer().getPluginCommand("kitpackmanager").setExecutor(commands);


        this.getSLF4JLogger().info(this.getName() + ' ' + "enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        this.getSLF4JLogger().info(this.getName() + ' ' + "disabled");
    }

}
