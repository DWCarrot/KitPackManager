package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.storage.CachedFileDatabase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReloadFunc implements ISubCommand {

    public static final String SUBCMD = "reload";


    private final Plugin plugin;

    private final CachedFileDatabase db;

    public ReloadFunc(Plugin plugin, CachedFileDatabase db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = (Player) commandSender;
        this.db.reloadAll(
                new CompletionHandler<Set<UUID>, Plugin>() {
                    @Override
                    public void completed(Set<UUID> result, Plugin attachment) {
                        player.sendMessage(String.format("reloaded %d kit-packs!", result.size()));
                        player.sendMessage(String.join(", ", result.stream().map(UUID::toString).collect(Collectors.toUnmodifiableList())));
                    }

                    @Override
                    public void failed(Throwable exc, Plugin attachment) {
                        attachment.getSLF4JLogger().error("database reload error", exc);
                    }
                },
                this.plugin
            );
        return true;
    }

    @Override
    public List<String> invokeTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        if(commandSender instanceof Player && commandSender.hasPermission("kitpackmanager.op")) {
            return true;
        }
        return false;
    }

    @Override
    public String getHelp(String main) {
        return main + ' ' + SUBCMD;
    }
}
