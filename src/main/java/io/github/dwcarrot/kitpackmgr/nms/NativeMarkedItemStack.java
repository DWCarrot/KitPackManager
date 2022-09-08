package io.github.dwcarrot.kitpackmgr.nms;

import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.logging.Logger;

public class NativeMarkedItemStack extends NativeItemStack {

    public static final String NBT_UUID_KEY = "kitpackmanager:uuid";

    protected static NativeItemStack from(net.minecraft.world.item.ItemStack inner) {
        net.minecraft.nbt.CompoundTag tag = inner.getTag();
        if(tag != null) {
            net.minecraft.nbt.Tag uuidStr = tag.get(NBT_UUID_KEY);
            if(uuidStr != null) {
                UUID uuid = UUID.fromString(uuidStr.getAsString());
                return new NativeMarkedItemStack(uuid, inner);
            }
        }
        return new NativeItemStack(inner);
    }

    public static NativeItemStack fromNBT(net.minecraft.nbt.CompoundTag nbt) {
        net.minecraft.world.item.ItemStack inner = net.minecraft.world.item.ItemStack.of(nbt);
        return from(inner);
    }

    public static NativeItemStack fromItemStack(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack inner = CraftItemStack.asNMSCopy(itemStack);
        return from(inner);
    }

    public final UUID uuid;

    protected NativeMarkedItemStack(UUID uuid, net.minecraft.world.item.ItemStack inner) {
        super(inner);
        this.uuid = uuid;
        net.minecraft.nbt.CompoundTag tag = this.inner.getOrCreateTag();
        tag.putString(NBT_UUID_KEY, this.uuid.toString());
    }
}
