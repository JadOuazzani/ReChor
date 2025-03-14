package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;
import static java.lang.Math.scalb;

public final class BufferedStations implements Stations {

    public static final int NAME_ID = 0;
    public static final int LON = 1;
    public static final int LAG = 2;
    StructuredBuffer structuredBuffer;
    List<String> stringTable;
    private static final double CONSTANTE = scalb(360d, -32);

    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID, U16), Structure.field(LON, S32), Structure.field(LAG, S32)), buffer);
    }

    @Override
    public String name(int id) throws IndexOutOfBoundsException {
        return stringTable.get(structuredBuffer.getU16(NAME_ID, id));
    }

    @Override
    public double longitude(int id) throws IndexOutOfBoundsException {
        return structuredBuffer.getS32(LON, id) * CONSTANTE;
    }

    @Override
    public double latitude(int id) throws IndexOutOfBoundsException {
        return structuredBuffer.getS32(LAG, id) * CONSTANTE;
    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
