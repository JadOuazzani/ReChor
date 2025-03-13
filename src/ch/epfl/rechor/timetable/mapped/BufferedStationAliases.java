package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;

public class BufferedStationAliases implements StationAliases {

    private final int ALIAS_ID = 0;
    private final int STATION_NAME_ID = 1;

    private List<String> stringTable;
    private StructuredBuffer structuredBuffer;

    BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        structuredBuffer = new StructuredBuffer(new Structure(Structure.field(ALIAS_ID, U16), Structure.field(STATION_NAME_ID, U16)), buffer);
    }

    @Override
    public String alias(int id) throws IndexOutOfBoundsException {
        return stringTable.get(structuredBuffer.getU16(ALIAS_ID, id));
    }

    @Override
    public String stationName(int id) throws IndexOutOfBoundsException {
        return stringTable.get(structuredBuffer.getU16(STATION_NAME_ID, id));
    }

    @Override
    public int size() {
        return this.size();
    }
}