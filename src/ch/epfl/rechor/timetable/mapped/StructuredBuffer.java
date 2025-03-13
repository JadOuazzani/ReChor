package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.nio.ByteBuffer;
import java.util.List;



public class StructuredBuffer {

    private ByteBuffer buffer;
    private Structure structure;

    StructuredBuffer(Structure structure, ByteBuffer buffer){
        Preconditions.checkArgument(buffer.capacity() % structure.totalsize() ==0);
        this.structure=structure;
        this.buffer = buffer;

    }

    public int size() {
        return buffer.capacity()/structure.totalsize();
    }

    public int getU8(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Byte.toUnsignedInt(buffer.get(offset));
    }

    public int getU16(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    int getS32(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return buffer.getInt(offset);

    }




}
