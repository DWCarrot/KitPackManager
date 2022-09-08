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

    List<NativeItemStack> items;

    public KitPack() {
        this.bind = null;
        this.items = null;
    }

    public KitPack(NativeMarkedItemStack itemStack) {
        this.bind = itemStack;
        this.items = null;
    }

    public KitPack(NativeMarkedItemStack bind, List<NativeItemStack> items) {
        this.bind = bind;
        this.items = items;
    }

    public NativeMarkedItemStack getBind() {
        return this.bind;
    }

    public List<NativeItemStack> getItems() {
        return this.items;
    }

    public boolean addItem(NativeItemStack itemStack) {
        if(this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(itemStack);
        return true;
    }
}
