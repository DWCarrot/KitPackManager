package io.github.dwcarrot.kitpackmgr.storage;
import io.github.dwcarrot.kitpackmgr.nms.NativeItemStack;
import io.github.dwcarrot.kitpackmgr.nms.NativeMarkedItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * store as json
 * - bind: NBTJson
 * - items: [NBTJson]
 * - commands: [String]
 * - rules: []
 */

public class KitPack {

    private static final UUID INVALID_UUID = new UUID(0, 0);

    public static final KitPack EMPTY = new KitPack();

    public static final int NBT_LIMIT = 65535;

    NativeMarkedItemStack bind;

    Selector selector;

    List<NativeItemStack> items;

    List<String> commands;

    public KitPack() {
        this.bind = null;
        this.items = null;
        this.selector = null;
    }

    public KitPack(NativeMarkedItemStack itemStack) {
        this(itemStack, new Selector(), new ArrayList<>(), new ArrayList<>());
    }

    public KitPack(NativeMarkedItemStack bind, Selector selector, List<NativeItemStack> items, List<String> commands) {
        this.bind = bind;
        this.items = items;
        this.selector = selector;
        this.commands = commands;
        this.bind.setAmountOne();
    }

    public NativeMarkedItemStack getBind() {
        return this.bind;
    }

    public boolean setBind(NativeMarkedItemStack bind) {
        this.bind = bind;
        this.bind.setAmountOne();
        return true;
    }

    public Selector getSelector() {
        return this.selector;
    }

    public boolean setSelector(Selector selector) {
        this.selector = selector;
        return true;
    }

    public List<NativeItemStack> getItems() {
        return this.items;
    }

    public boolean addItem(NativeItemStack itemStack, int index) {
        if(index >= 0 && index < this.items.size()) {
            this.items.add(index, itemStack);
        } else {
            this.items.add(itemStack);
        }
        return true;
    }

    public boolean removeItem(int index) {
        if(index >= 0 && index < this.items.size()) {
            this.items.remove(index);
            return true;
        } else {
            return false;
        }
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public boolean addCommand(String command, int index) {
        if(index >= 0 && index < this.items.size()) {
            this.commands.add(index, command);
        } else {
            this.commands.add(command);
        }
        return true;
    }

    public boolean removeCommand(int index) {
        if(index >= 0 && index < this.items.size()) {
            this.commands.remove(index);
            return true;
        } else {
            return false;
        }
    }
}
