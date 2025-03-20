package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U8;

public final class BufferedTrips implements Trips {

    private final int ROUTE_ID = 0;
    private final int DESTINATION_ID = 1;

    private List<String> stringTable;
    private StructuredBuffer structuredBuffer;

    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        structuredBuffer = new StructuredBuffer(new Structure(Structure.field(ROUTE_ID, U16), Structure.field(DESTINATION_ID, U16)), buffer);
    }

    @Override
    public int routeId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer.getU16(ROUTE_ID, id);
    }

    @Override
    public String destination(int id) throws IndexOutOfBoundsException {
        return stringTable.get(structuredBuffer.getU16(DESTINATION_ID, id));
    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
