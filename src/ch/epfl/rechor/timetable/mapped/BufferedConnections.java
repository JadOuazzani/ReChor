package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;

public final class BufferedConnections implements Connections {

    private final int DEP_STOP_ID = 0;
    private final int DEP_MINUTES = 1;
    private final int ARR_STOP_ID = 2;
    private final int ARR_MINUTES = 3;
    private final int TRIP_POS_ID = 4;

    private StructuredBuffer structuredBuffer1;
    private IntBuffer structuredBuffer2;

    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {
        structuredBuffer1 = new StructuredBuffer(new Structure(Structure.field(DEP_STOP_ID, U16),
                Structure.field(DEP_MINUTES, U16),
                Structure.field(ARR_STOP_ID, U16),
                Structure.field(ARR_MINUTES, U16),
                Structure.field(TRIP_POS_ID, S32)),
                buffer);
        structuredBuffer2 = succBuffer.asIntBuffer();
    }


    @Override
    public int depStopId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getU16(DEP_STOP_ID, id);
    }

    @Override
    public int depMins(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getU16(DEP_MINUTES, id);
    }

    @Override
    public int arrStopId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getU16(ARR_STOP_ID, id);
    }

    @Override
    public int arrMins(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getU16(ARR_MINUTES, id);
    }

    @Override
    public int tripId(int id) throws IndexOutOfBoundsException { // retourne l'index de la course à laquelle appartient la liaison d'index donné
        return structuredBuffer1.getS32(TRIP_POS_ID, id) >>> 8;
    }

    @Override
    public int tripPos(int id) throws IndexOutOfBoundsException { // retourne la position de la liaison d'index donné dans la course à laquelle elle appartient, la première liaison d'une course ayant l'index 0
        return structuredBuffer1.getS32(TRIP_POS_ID, id) & 0xFF;
    }

    @Override
    public int nextConnectionId(int id) throws IndexOutOfBoundsException { // retourne l'index de la liaison suivant celle d'index donné dans la course à laquelle elle appartient, ou l'index de la première liaison de la course si la liaison d'index donné est la dernière de la course.
        return structuredBuffer2.get(id);
    }

    @Override
    public int size() {
        return structuredBuffer1.size();
    }
}
