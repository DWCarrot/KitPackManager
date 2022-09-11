package io.github.dwcarrot.kitpackmgr.nms;

import com.google.common.collect.Lists;
import io.github.dwcarrot.kitpackmgr.storage.KitPack;
import io.github.dwcarrot.kitpackmgr.storage.Selector;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KitPackConverter implements ICompoundConverter<KitPack> {
    @Override
    public CompoundTag convert(KitPack value) throws Exception {
        CompoundTag root = new CompoundTag();
        CompoundTag bind = new CompoundTag();
        value.getBind().inner.save(bind);
        root.put("bind", bind);
        ListTag items = new ListTag();
        for(NativeItemStack itemStack : value.getItems()) {
            CompoundTag item = new CompoundTag();
            itemStack.inner.save(item);
            items.add(item);
        }
        root.put("items", items);
        ListTag commands = new ListTag();
        for(String command : value.getCommands()) {
            commands.add(StringTag.valueOf(command));
        }
        root.put("commands", commands);
        StringTag selector = StringTag.valueOf(value.getSelector().getRaw());
        root.put("selector", selector);
        return root;
    }

    @Override
    public KitPack convertBack(CompoundTag raw) throws Exception {
        CompoundTag bindTag = raw.getCompound("bind");
        NativeMarkedItemStack bind = (NativeMarkedItemStack) NativeMarkedItemStack.fromNBT(bindTag);
        List<NativeItemStack> items = raw.getList("items", Tag.TAG_COMPOUND)
                .stream()
                .map(NativeMarkedItemStack::fromNBTUnchecked)
                .collect(Collectors.toCollection(ArrayList::new));
        List<String> commands = raw.getList("commands", Tag.TAG_STRING)
                .stream()
                .map(Tag::getAsString)
                .collect(Collectors.toCollection(ArrayList::new));
        Selector selector;
        String s = raw.getString("selector");
        if(s.isEmpty()) {
            selector = new Selector();
        } else {
            selector = new Selector(s);
        }
        return new KitPack(bind, selector, items, commands);
    }
}
