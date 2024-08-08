package io.github.dwcarrot.kitpackmgr.nms;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;


public class NBTFile<T> {

    protected final long limit;

    protected final ICompoundConverter<T> converter;

    public NBTFile(long limit, ICompoundConverter<T> converter) {
        this.limit = limit;
        this.converter = converter;
    }

    public T read(InputStream inputStream) throws Exception {
        try(DataInputStream idata = new DataInputStream(inputStream)) {
            if(Tag.TAG_COMPOUND != idata.readByte())
                throw new ReportedException(new CrashReport("root is not a compound", null));
            int len = (int)idata.readShort();
            if(len > 0) {
                byte[] buffer = idata.readNBytes(len);
                String name = new String(buffer, StandardCharsets.UTF_8);
            }
            CompoundTag compound = CompoundTag.TYPE.load(idata, NbtAccounter.create(this.limit));
            return this.converter.convertBack(compound);
        }
    }

    public void write(OutputStream outputStream, T value) throws Exception {
        try(DataOutputStream odata = new DataOutputStream(outputStream)) {
            CompoundTag compound = this.converter.convert(value);
            /*
            compound.write will be invoked recursively
            so the compound-type-id and label will not be added.
            write root compound type-id and label here.
             */
            odata.write(Tag.TAG_COMPOUND);
            odata.writeShort(0);
            compound.write(odata);
        }
    }
}
