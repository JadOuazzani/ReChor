package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import static ch.epfl.rechor.PackedRange.*;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U8;

/**
 * BufferedTransfers is a final class that implements the {@link Transfers} interface.
 * <p>
 * It provides access to a flattened table of transfers as described in §2.5.
 * The transfer data is stored in a {@link ByteBuffer} and accessed via a {@link StructuredBuffer}.
 * Additionally, an integer array is built to store, for each arrival station, the packed interval (range)
 * of transfer indices that arrive at that station.
 * </p>
 * <p>
 * The flattened transfer data is structured with three fields:
 * <ul>
 *   <li>Field 0 (DEP_STATION_ID, U16): the departure station index,</li>
 *   <li>Field 1 (ARR_STATION_ID, U16): the arrival station index,</li>
 *   <li>Field 2 (TRANSFER_MINUTE, U8): the transfer duration in minutes.</li>
 * </ul>
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class BufferedTransfers implements Transfers {

    private final int DEP_STATION_ID = 0;
    private final int ARR_STATION_ID = 1;
    private final int TRANSFER_MINUTE = 2;

    private final StructuredBuffer structuredBuffer;
    private final int[] tableTransfers;

    /**
     * Constructs a BufferedTransfers instance.
     * <p>
     * The provided {@code buffer} contains the flattened transfer data.
     * The constructor initializes a {@link StructuredBuffer} using a structure defined with:
     * <ul>
     *   <li>DEP_STATION_ID (U16),</li>
     *   <li>ARR_STATION_ID (U16),</li>
     *   <li>TRANSFER_MINUTE (U8).</li>
     * </ul>
     * It then builds an array {@code tableTransfers} that, for each arrival station, holds the packed interval
     * of transfer indices (using {@link ch.epfl.rechor.PackedRange#pack(int, int)} along with
     * {@link ch.epfl.rechor.PackedRange#startInclusive(int)} and {@link ch.epfl.rechor.PackedRange#endExclusive(int)}).
     * </p>
     *
     * @param buffer the ByteBuffer containing the flattened transfer data
     */
    public BufferedTransfers(ByteBuffer buffer) {
        this.structuredBuffer = new StructuredBuffer(new Structure(
                Structure.field(DEP_STATION_ID, U16),
                Structure.field(ARR_STATION_ID, U16),
                Structure.field(TRANSFER_MINUTE, U8)), buffer);

        int n = structuredBuffer.size();
        int maxStation = 0;

        // --- PASSAGE 1: Determine the highest arrival station index ---
        for (int i = 0; i < n; i++) {
            int currentArrStationId = structuredBuffer.getU16(ARR_STATION_ID, i);
            if (maxStation < currentArrStationId) {
                maxStation = currentArrStationId;
            }
        }

        tableTransfers = new int[maxStation + 1];

        // --- PASSAGE 2: Build the intervals for each station ---
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

    /**
     * Returns the departure station index for the transfer at the specified index.
     *
     * @param id the index of the transfer
     * @return the departure station index (as an unsigned 16-bit value)
     * @throws IndexOutOfBoundsException if {@code id} is invalid
     */
    @Override
    public int depStationId(int id) {
        return structuredBuffer.getU16(DEP_STATION_ID, id);
    }

    /**
     * Returns the transfer duration (in minutes) for the transfer at the specified index.
     *
     * @param id the index of the transfer
     * @return the transfer duration in minutes (as an unsigned 8-bit value)
     * @throws IndexOutOfBoundsException if {@code id} is invalid
     */
    @Override
    public int minutes(int id){
        return structuredBuffer.getU8(TRANSFER_MINUTE, id);
    }

    /**
     * Returns the packed interval of transfer indices for the arrival station specified.
     * <p>
     * The returned interval is packed according to the {@link ch.epfl.rechor.PackedRange} convention.
     * </p>
     *
     * @param stationId the index of the arrival station
     * @return the packed interval (range) of transfer indices arriving at the given station
     * @throws IndexOutOfBoundsException if {@code stationId} is invalid
     */
    @Override
    public int arrivingAt(int stationId) {
        return tableTransfers[stationId];
    }

    /**
     * Returns the transfer duration (in minutes) for a transfer from a specified departure station
     * to a specified arrival station.
     * <p>
     * This method searches within the packed interval (range) for the arrival station for a transfer
     * whose departure station matches the given {@code depStationId}, and returns the corresponding transfer duration.
     * If no such transfer exists, a {@link NoSuchElementException} is thrown.
     * </p>
     *
     * @param depStationId the departure station index
     * @param arrStationId the arrival station index
     * @return the transfer duration in minutes
     * @throws NoSuchElementException if no transfer exists between the given stations
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
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

    /**
     * Returns the number of transfers in the flattened transfer table.
     *
     * @return the number of transfers
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
