package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.nio.ByteBuffer;

/**
 * Represents a structured byte buffer.
 * <p>
 * A {@code StructuredBuffer} provides convenient access to flattened data stored in a {@link ByteBuffer},
 * whose structure is described by an instance of {@link Structure}. For example, once a StructuredBuffer is created,
 * it is possible to obtain, for instance, the U16 value for a particular field and element:
 * <pre>
 * StructuredBuffer buffer = …;
 * int stationNameStringIndex = buffer.getU16(STATION_NAME_ID, 1);
 * </pre>
 * In the example from §2.6, {@code stationNameStringIndex} would be 3, since it is the string index for "Ins".
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public class StructuredBuffer {

    private ByteBuffer buffer;
    private Structure structure;

    /**
     * Constructs a StructuredBuffer with the given structure and underlying ByteBuffer.
     * <p>
     * The provided {@code ByteBuffer} must have a capacity that is a multiple of the total size of the structure;
     * otherwise, an {@code IllegalArgumentException} is thrown.
     * </p>
     *
     * @param structure the structure describing the layout of the flattened data
     * @param buffer    the ByteBuffer containing the flattened data
     * @throws IllegalArgumentException if the buffer's capacity is not a multiple of {@code structure.totalSize()}
     */
    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        Preconditions.checkArgument(buffer.capacity() % structure.totalSize() == 0);
        this.structure = structure;
        this.buffer = buffer;
    }

    /**
     * Returns the number of elements in the structured buffer.
     * <p>
     * The number of elements is determined by dividing the buffer's capacity by the total size of the structure.
     * </p>
     *
     * @return the number of elements contained in the buffer
     */
    public int size() {
        return buffer.capacity() / structure.totalSize();
    }

    /**
     * Returns the unsigned 8-bit value corresponding to the field at the given index of the specified element.
     * <p>
     * The value is interpreted as an unsigned integer in the range [0, 255].
     * </p>
     *
     * @param fieldIndex   the index of the field in the structure
     * @param elementIndex the index of the element in the buffer
     * @return the unsigned 8-bit value of the field
     * @throws IndexOutOfBoundsException if either index is invalid
     */
    public int getU8(int fieldIndex, int elementIndex) {
        return Byte.toUnsignedInt(buffer.get(structure.offset(fieldIndex, elementIndex)));
    }

    /**
     * Returns the unsigned 16-bit value corresponding to the field at the given index of the specified element.
     * <p>
     * The value is interpreted as an unsigned integer in the range [0, 65535].
     * </p>
     *
     * @param fieldIndex   the index of the field in the structure
     * @param elementIndex the index of the element in the buffer
     * @return the unsigned 16-bit value of the field
     * @throws IndexOutOfBoundsException if either index is invalid
     */
    public int getU16(int fieldIndex, int elementIndex) {
        return Short.toUnsignedInt(buffer.getShort(structure.offset(fieldIndex, elementIndex)));
    }

    /**
     * Returns the signed 32-bit value corresponding to the field at the given index of the specified element.
     *
     * @param fieldIndex   the index of the field in the structure
     * @param elementIndex the index of the element in the buffer
     * @return the signed 32-bit value of the field
     * @throws IndexOutOfBoundsException if either index is invalid
     */
    public int getS32(int fieldIndex, int elementIndex) {
        return buffer.getInt(structure.offset(fieldIndex, elementIndex));
    }
}
