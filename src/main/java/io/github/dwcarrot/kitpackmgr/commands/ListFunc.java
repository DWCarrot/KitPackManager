package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.controls.Operation;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class ListFunc implements ISubCommand {

    public static final String SUBCMD = "list";

    private final Plugin plugin;

    private final Operation db;

    public ListFunc(Plugin plugin, Operation db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args, int argsOffset) {
        this.db.list(new ShowList(commandSender), this.plugin);
        return true;
    }

    @Override
    public List<String> invokeTabComplete(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
        return null;
    }

    @Override
    public boolean checkPermission(CommandSender commandSender) {
        if(commandSender.hasPermission("kitpackmanager.op")) {
            return true;
        }
        return false;
    }

    @Override
    public String getHelp(String main) {
        return main + ' ' + SUBCMD;
    }
}

class ShowList implements BiConsumer<Collection<KitPack>, Plugin> {

    private final CommandSender commandSender;

    ShowList(CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    @Override
    public void accept(Collection<KitPack> kitPacks, Plugin plugin) {
        TextColor uuidColor = TextColor.color(0x55, 0xFF, 0xFF);
        Component sp = Component.text(' ');
        int index = 0;
        for(KitPack kitPack : kitPacks) {
            String uuidString = kitPack.getBind().uuid.toString();
            Component head = Component.text(String.format("[%d]", index++));
            Component uuid = Component.text(uuidString, uuidColor)
                    .hoverEvent(HoverEvent.showText(Component.text("click to copy UUID")))
                    .clickEvent(ClickEvent.copyToClipboard(uuidString));
            Component item = InventoryUtils.showItem(kitPack.getBind().unwrap());
            this.commandSender.sendMessage(head.append(sp).append(uuid).append(sp).append(item));
        }
    }
}