package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.nio.ByteBuffer;

public class StructuredBuffer {

    private ByteBuffer buffer;
    private Structure structure;

    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        Preconditions.checkArgument(buffer.capacity() % structure.totalSize() == 0);
        this.structure = structure;
        this.buffer = buffer;
    }

    public int size() {
        return buffer.capacity() / structure.totalSize();
    }

    public int getU8(int fieldIndex, int elementIndex) {
        return Byte.toUnsignedInt(buffer.get(structure.offset(fieldIndex, elementIndex)));
    }

    public int getU16(int fieldIndex, int elementIndex) {
        return Short.toUnsignedInt(buffer.getShort(structure.offset(fieldIndex, elementIndex)));
    }

    public int getS32(int fieldIndex, int elementIndex) {
        return buffer.getInt(structure.offset(fieldIndex, elementIndex));

    }


}
