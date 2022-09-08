package io.github.dwcarrot.kitpackmgr.nms;

import com.google.common.collect.Lists;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.commands.TagCommand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitPackConverter implements ICompoundConverter<KitPack> {
    @Override
    public CompoundTag convert(KitPack value) throws Exception {
        CompoundTag root = new CompoundTag();
        CompoundTag bind = new CompoundTag();
        value.getBind().inner.save(bind);
        root.put("bind", bind);
        ListTag items = new ListTag();
        List<NativeItemStack> itemStackList = value.getItems();
        if(itemStackList != null) {
            for(NativeItemStack itemStack : itemStackList) {
                CompoundTag item = new CompoundTag();
                itemStack.inner.save(item);
                items.add(item);
            }
        }
        root.put("items", items);
        return root;
    }

    @Override
    public KitPack convertBack(CompoundTag raw) throws Exception {
        CompoundTag bind = raw.getCompound("bind");
        NativeMarkedItemStack markedItemStack = (NativeMarkedItemStack) NativeMarkedItemStack.fromNBT(bind);
        ListTag items = raw.getList("items", Tag.TAG_COMPOUND);
        List<NativeItemStack> itemStackList = new ArrayList<>();
        if(items != null) {
            for(Tag item : items) {
                CompoundTag tag = (CompoundTag)item;
                ItemStack itemStack = ItemStack.of(tag);
                itemStackList.add(new NativeItemStack(itemStack));
            }
        }
        return new KitPack(markedItemStack, itemStackList);
    }
}
