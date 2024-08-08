package io.github.dwcarrot.kitpackmgr.nms;

import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class NativeItemStack {

    protected final net.minecraft.world.item.ItemStack inner;

    public NativeItemStack(ItemStack itemStack) {
        this.inner = CraftItemStack.asNMSCopy(itemStack);
    }

    protected NativeItemStack(net.minecraft.world.item.ItemStack inner) {
        this.inner = inner;
    }

    public ItemStack unwrap() {
        return CraftItemStack.asBukkitCopy(this.inner);
    }

    public int count() {
        return this.inner.getCount();
    }

    public NativeMarkedItemStack set(UUID uuid) {
        return new NativeMarkedItemStack(uuid, this.inner);
    }
}
