package ch.epfl.rechor.timetable.mapped;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Structure {
    private List<Field> field;


    public enum FieldType {
        U8,
        U16,
        S32;
    }

    public record Field(int index, FieldType type) {
        public Field {
            if (type == null) {throw new NullPointerException();}
        }

    }

    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    public Structure(Field... fields) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].index() != i) {
                throw new IllegalArgumentException();
            }

        }
        field = new ArrayList<>();
        this.field.addAll(Arrays.asList(fields));

        
    }

    int totalsize() {

        int totalSize = 0;

        for (Field field : field) {
            switch (field.type()) {
                case U8:
                    totalSize += 1;
                    break;
                case U16:
                    totalSize += 2;
                    break;
                case S32:
                    totalSize += 4;
                    break;
            }
        }

        return totalSize;
    }

    }

