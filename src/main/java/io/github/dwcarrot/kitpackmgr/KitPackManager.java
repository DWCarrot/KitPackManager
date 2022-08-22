package io.github.dwcarrot.kitpackmgr;

import io.github.dwcarrot.kitpackmgr.events.RightClickHandler;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class KitPackManager extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getLogger().info("KitPackManager enabled!");
        this.getServer().getPluginManager().registerEvents(new RightClickHandler(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        this.getLogger().info("KitPackManager disabled!");
    }

}
