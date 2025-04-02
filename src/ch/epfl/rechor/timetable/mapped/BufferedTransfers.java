package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import static ch.epfl.rechor.PackedRange.*;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U8;

public final class BufferedTransfers implements Transfers {

    private final int DEP_STATION_ID = 0;
    private final int ARR_STATION_ID = 1;
    private final int TRANSFER_MINUTE = 2;

    private final StructuredBuffer structuredBuffer;
    private final int[] tableTransfers;

    public BufferedTransfers(ByteBuffer buffer) {
        this.structuredBuffer = new StructuredBuffer(new Structure(
                Structure.field(DEP_STATION_ID, U16),
                Structure.field(ARR_STATION_ID, U16),
                Structure.field(TRANSFER_MINUTE, U8)), buffer);

        int n = structuredBuffer.size();
        int maxStation = 0;

        // --- PASSAGE 1 : déterminer la plus grande gare d'arrivée ---
        for (int i = 0; i < n; i++) {
            int currentArrStationId = structuredBuffer.getU16(ARR_STATION_ID, i);
            if (maxStation < currentArrStationId) {
                maxStation = currentArrStationId;
            }
        }

        tableTransfers = new int[maxStation + 1];

        // --- PASSAGE 2 : construire les intervalles pour chaque gare ---
        if (n > 0) {
            int startInclusive = 0;
            int currentStation = structuredBuffer.getU16(ARR_STATION_ID, 0);

            for (int i = 1; i < n; i++) {
                int arr = structuredBuffer.getU16(ARR_STATION_ID, i);
                if (currentStation != arr) {
                    tableTransfers[currentStation] = pack(startInclusive, i);
                    currentStation = arr;
                    startInclusive = i;
                }
            }
            tableTransfers[currentStation] = pack(startInclusive, n);
        }
    }

    @Override
    public int depStationId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer.getU16(DEP_STATION_ID, id);
    }

    @Override
    public int minutes(int id) throws IndexOutOfBoundsException {
        return structuredBuffer.getU8(TRANSFER_MINUTE, id);
    }

    @Override
    public int arrivingAt(int stationId) throws IndexOutOfBoundsException {
        return tableTransfers[stationId];
    }

    @Override
    public int minutesBetween(int depStationId, int arrStationId)  {
        int range = arrivingAt(arrStationId);
        int start = startInclusive(range);
        int end = endExclusive(range);

        for (int i = start; i < end; i++) {
            if (depStationId(i) == depStationId) {
                return minutes(i);
            }
        }

        throw new NoSuchElementException();
    }

    @Override
    public int size() {
        return structuredBuffer.size();
    }
}