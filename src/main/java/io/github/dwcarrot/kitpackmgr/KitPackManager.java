package io.github.dwcarrot.kitpackmgr;

import io.github.dwcarrot.kitpackmgr.commands.Commands;
import io.github.dwcarrot.kitpackmgr.events.RightClickHandler;
import io.github.dwcarrot.kitpackmgr.storage.CachedFileDatabase;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class KitPackManager extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        FileConfiguration cfg = this.getConfig();
        boolean modified = false;
        String key1 = "placeholder-player-name";
        String value1 = cfg.getString(key1, "");
        if(value1.isEmpty()) {
            cfg.set(key1, RightClickHandler.PlaceholderPlayerName);
            modified = true;
        } else {
            RightClickHandler.PlaceholderPlayerName = value1;
        }
        String key2 = "execute-delay";
        long value2 = cfg.getLong(key2, -1);
        if(value2 < 0) {
            cfg.set(key2, RightClickHandler.ExecuteDelay);
            modified = true;
        } else {
            RightClickHandler.ExecuteDelay = value2;
        }
        if(modified) {
            try {
                cfg.save(new File(this.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                this.getSLF4JLogger().error("save config fail", e);
            }
        }

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
