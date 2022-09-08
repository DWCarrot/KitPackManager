package io.github.dwcarrot.kitpackmgr.nms;

public interface ICompoundConverter<T> {

    net.minecraft.nbt.CompoundTag convert(T value) throws Exception;

    T convertBack(net.minecraft.nbt.CompoundTag raw) throws Exception;
}
