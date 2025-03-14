package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Structure {
    private List<Field> field;
    private int totalSize = 0;
    private final int[] fieldOffsets;

    public enum FieldType {
        U8,
        U16,
        S32;
    }

    public record Field(int index, FieldType type) {
        public Field {
            Objects.requireNonNull(type);
        }

    }

    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

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

    public int totalSize() {
        return totalSize;
    }

    public int offset(int fieldIndex, int elementIndex) {
        return elementIndex * totalSize() + fieldOffsets[fieldIndex];
    }


}

