package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Facilitates the description of flattened data structures.
 * <p>
 * A Structure instance defines the layout of flattened data by specifying a set of fields.
 * Each field is defined by an index and a type (one of U8, U16, or S32). The Structure
 * allows computing the offset (in bytes) of a given field within a flattened array of data.
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public class Structure {
    private List<Field> field;
    private int totalSize = 0;
    private final int[] fieldOffsets;

    /**
     * Enumeration of field types.
     * <p>
     * The three possible field types are:
     * <ul>
     *   <li>{@code U8} – an 8-bit unsigned field.</li>
     *   <li>{@code U16} – a 16-bit unsigned field.</li>
     *   <li>{@code S32} – a 32-bit signed field.</li>
     * </ul>
     * </p>
     */
    public enum FieldType {
        U8,
        U16,
        S32;
    }

    /**
     * Represents a field in a Structure.
     * <p>
     * A field is characterized by its index (its position in the structure) and its type.
     * The constructor ensures that the type is not null.
     * </p>
     *
     * @param index the index of the field (starting at 0)
     * @param type  the type of the field, must not be null
     */
    public record Field(int index, FieldType type) {
        public Field {
            Objects.requireNonNull(type);
        }
    }

    /**
     * Convenience method to create a new Field.
     *
     * @param index the index of the field
     * @param type  the type of the field
     * @return a new Field instance with the given index and type
     */
    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    /**
     * Constructs a new Structure from the given fields.
     * <p>
     * The fields must be provided in order; that is, the first field must have index 0,
     * the second index 1, and so on. This constructor computes the offset for each field
     * based on its type (U8: 1 byte, U16: 2 bytes, S32: 4 bytes) and the total size of the structure.
     * </p>
     *
     * @param fields the fields defining the structure
     * @throws IllegalArgumentException if the fields are not provided in order
     */
    public Structure(Field... fields) {
        for (int i = 0; i < fields.length; i++) {
            Preconditions.checkArgument(fields[i].index() == i);
        }

        field = new ArrayList<>();
        this.field.addAll(Arrays.asList(fields));

        this.fieldOffsets = new int[fields.length];
        int currentSize = 0;
        int i = 0;

        for (Field field : fields) {
            fieldOffsets[i] = currentSize;
            switch (field.type()) {
                case U8:
                    currentSize += 1;
                    break;
                case U16:
                    currentSize += 2;
                    break;
                case S32:
                    currentSize += 4;
                    break;
            }
            i += 1;
        }

        totalSize = currentSize;
    }

    /**
     * Returns the total size of the structure in bytes.
     *
     * @return the total size (in bytes) of the structure
     */
    public int totalSize() {
        return totalSize;
    }

    /**
     * Computes the offset (in bytes) in the flattened data array for the given field and element index.
     * <p>
     * The offset is calculated by multiplying the element index by the total size of the structure,
     * and then adding the offset of the specified field.
     * </p>
     *
     * @param fieldIndex   the index of the field (must be valid)
     * @param elementIndex the index of the element in the flattened data array
     * @return the offset (in bytes) of the specified field of the specified element
     * @throws IndexOutOfBoundsException if the field index is invalid
     */
    public int offset(int fieldIndex, int elementIndex) {
        return elementIndex * totalSize() + fieldOffsets[fieldIndex];
    }
}
