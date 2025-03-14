package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;

public final class BufferedPlatforms implements Platforms {
    private final int NAME_ID = 0;
    private final int STATION_ID = 1;
    private List<String> stringTable;
    private StructuredBuffer structuredBuffer;

    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        structuredBuffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID, U16), Structure.field(STATION_ID, U16)), buffer);


    }

    @Override
    public String name(int id) throws IndexOutOfBoundsException {
        return stringTable.get(structuredBuffer.getU16(NAME_ID, id));
    }

    @Override
    public int stationId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer.getU16(STATION_ID, id);
    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }
}