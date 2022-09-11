package io.github.dwcarrot.kitpackmgr.commands;

import io.github.dwcarrot.kitpackmgr.controls.Operation;
import io.github.dwcarrot.kitpackmgr.nms.NativeItemStack;
import io.github.dwcarrot.kitpackmgr.nms.NativeMarkedItemStack;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import io.github.dwcarrot.kitpackmgr.storage.Selector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SelectorFunc implements ISubCommand {

    public static final String SUBCMD = "selector";

    private final Plugin plugin;

    private final Operation db;

    public SelectorFunc(Plugin plugin, Operation db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean invokeCommand(CommandSender commandSender, Command command, String label, String[] args, int argsOffset) {
        Player player = (Player) commandSender;
        ItemStack itemStack;
        itemStack = InventoryUtils.getMainHandItem(player);
        if (itemStack == null) {
            return true;
        }
        int amount = itemStack.getAmount();
        NativeItemStack w = NativeMarkedItemStack.fromItemStack(itemStack);
        if(!(w instanceof NativeMarkedItemStack)) {
            player.sendMessage("no kit-pack in main-hand");
        }
        final UUID uuid = ((NativeMarkedItemStack)w).uuid;
        if(args.length <= argsOffset) {
            // show
            this.db.retrieve(uuid, new ShowSelector(player, uuid), this.plugin);
        } else {
            // set
            Selector s;
            try {
                s = new Selector(args[argsOffset++]);
            } catch (ParseException e) {
                player.sendMessage(e.getMessage());
                return false;
            }
            this.db.modify(
                    uuid,
                    kitPack -> kitPack.setSelector(s),
                    new SetPlayerMainHand(player, amount, "kit-pack %s set selector success"),
                    this.plugin
                );
        }
        return true;
    }

    @Override
    public List<String> invokeTabComplete(CommandSender sender, Command command, String alias, String[] args, int argsOffset) {
        if(args.length > argsOffset) {
            if(args[argsOffset].isEmpty()) {
                return List.of("?<selector>");
            }
        }
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
        return main + ' ' + SUBCMD + ' ' + "[<selector>]";
    }
}

class ShowSelector implements BiConsumer<KitPack, Plugin> {

    private final Player player;
    private final UUID uuid;
    private final String formatFail = "unable to find kit-pack [%s]";

    ShowSelector(Player player, UUID uuid) {
        this.player = player;
        this.uuid = uuid;
    }

    @Override
    public void accept(KitPack kitPack, Plugin plugin) {
        if(kitPack != null) {
            TextColor c = TextColor.color(0x66, 0xCC, 0xFF);
            Selector s = kitPack.getSelector();
            Component head = Component.text("selector=");
            Component text = Component.text(s.getRaw(), c);
            if(!s.isEmpty()) {
                text = text
                        .clickEvent(ClickEvent.copyToClipboard(s.getRaw()))
                        .hoverEvent(HoverEvent.showText(Component.text("click to copy")));
            }
            player.sendMessage(head.append(text));
        } else {
            player.sendMessage(String.format(this.formatFail, this.uuid));
        }
    }
}