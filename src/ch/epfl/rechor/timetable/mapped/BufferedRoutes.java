package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U8;

public final class BufferedRoutes implements Routes {

    private final int NAME_ID = 0;
    private final int KIND = 1;
    private List<String> stringTable;
    private StructuredBuffer structuredBuffer;

    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        structuredBuffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID, U16), Structure.field(KIND, U8)), buffer);


    }
    @Override
    public Vehicle vehicle(int id) throws IndexOutOfBoundsException {
        Vehicle vehicle = null;
        switch (structuredBuffer.getU8(KIND, id)) {
            case 0:
                vehicle = Vehicle.TRAM;
                break;
            case 1 :
                vehicle = Vehicle.METRO;
                break;
            case 2 :
                vehicle = Vehicle.TRAIN;
                break;
            case 3 :
                vehicle = Vehicle.BUS;
                break;
            case 4 :
                vehicle = Vehicle.FERRY;
                break;
            case 5 :
                vehicle = Vehicle.AERIAL_LIFT;
                break;
            case 6  :
                vehicle = Vehicle.FUNICULAR;
                break;
        }

        return vehicle;
    }


    @Override
    public String name(int id) throws IndexOutOfBoundsException {
        return stringTable.get(structuredBuffer.getU16(NAME_ID, id));
    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
