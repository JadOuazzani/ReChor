package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;

/**
 * BufferedConnections provides access to a flattened table of connections.
 * <p>
 * This class implements the {@link Connections} interface and allows access to connection data
 * stored in two byte buffers. The first buffer contains the flattened connection data (as described in §§2.3 and 2.4),
 * and the second buffer contains the "next connection" indices.
 * </p>
 * <p>
 * The flattened connection data is structured with the following fields:
 * <ul>
 *   <li>{@code DEP_STOP_ID} (U16): the departure stop index,</li>
 *   <li>{@code DEP_MINUTES} (U16): the departure time in minutes after midnight,</li>
 *   <li>{@code ARR_STOP_ID} (U16): the arrival stop index,</li>
 *   <li>{@code ARR_MINUTES} (U16): the arrival time in minutes after midnight,</li>
 *   <li>{@code TRIP_POS_ID} (S32): an integer encoding both the trip ID (upper 24 bits) and the position within the trip (lower 8 bits).</li>
 * </ul>
 * The second buffer, interpreted as an {@link IntBuffer}, provides for each connection the index of the next connection.
 * </p>
 */
public final class BufferedConnections implements Connections {

    private final int DEP_STOP_ID = 0;
    private final int DEP_MINUTES = 1;
    private final int ARR_STOP_ID = 2;
    private final int ARR_MINUTES = 3;
    private final int TRIP_POS_ID = 4;

    private StructuredBuffer structuredBuffer1;
    private IntBuffer structuredBuffer2;

    /**
     * Constructs a BufferedConnections instance.
     * <p>
     * The first ByteBuffer contains the flattened connection data, structured with five fields:
     * <ul>
     *   <li>Departure stop ID (U16)</li>
     *   <li>Departure minutes (U16)</li>
     *   <li>Arrival stop ID (U16)</li>
     *   <li>Arrival minutes (U16)</li>
     *   <li>Trip position ID (S32) encoding both the trip ID and position.</li>
     * </ul>
     * The second ByteBuffer contains the next connection indices and is converted to an {@link IntBuffer}.
     * </p>
     *
     * @param buffer     the ByteBuffer containing the flattened connection data
     * @param succBuffer the ByteBuffer containing the indices of the next connection for each connection
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {
        structuredBuffer1 = new StructuredBuffer(
                new Structure(
                        Structure.field(DEP_STOP_ID, U16),
                        Structure.field(DEP_MINUTES, U16),
                        Structure.field(ARR_STOP_ID, U16),
                        Structure.field(ARR_MINUTES, U16),
                        Structure.field(TRIP_POS_ID, S32)
                ),
                buffer);
        structuredBuffer2 = succBuffer.asIntBuffer();
    }

    /**
     * Returns the departure stop index for the connection at the specified index.
     *
     * @param id the index of the connection
     * @return the departure stop index (U16)
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int depStopId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getU16(DEP_STOP_ID, id);
    }

    /**
     * Returns the departure time (in minutes after midnight) for the connection at the specified index.
     *
     * @param id the index of the connection
     * @return the departure time in minutes after midnight (U16)
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int depMins(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getU16(DEP_MINUTES, id);
    }

    /**
     * Returns the arrival stop index for the connection at the specified index.
     *
     * @param id the index of the connection
     * @return the arrival stop index (U16)
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int arrStopId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getU16(ARR_STOP_ID, id);
    }

    /**
     * Returns the arrival time (in minutes after midnight) for the connection at the specified index.
     *
     * @param id the index of the connection
     * @return the arrival time in minutes after midnight (U16)
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int arrMins(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getU16(ARR_MINUTES, id);
    }

    /**
     * Returns the trip ID for the connection at the specified index.
     * <p>
     * The trip ID is extracted from the S32 value stored in the field {@code TRIP_POS_ID} by shifting right 8 bits.
     * </p>
     *
     * @param id the index of the connection
     * @return the trip ID (upper bits of the S32 value)
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int tripId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getS32(TRIP_POS_ID, id) >>> 8;
    }

    /**
     * Returns the position of the connection within its trip.
     * <p>
     * The position is stored in the lower 8 bits of the S32 value in the field {@code TRIP_POS_ID}.
     * The first connection in a trip has position 0.
     * </p>
     *
     * @param id the index of the connection
     * @return the position of the connection within its trip
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int tripPos(int id) throws IndexOutOfBoundsException {
        return structuredBuffer1.getS32(TRIP_POS_ID, id) & 0xFF;
    }

    /**
     * Returns the index of the next connection in the same trip for the connection at the specified index.
     * <p>
     * If the connection at the given index is the last in its trip, this method returns the index of the first
     * connection of that trip.
     * </p>
     *
     * @param id the index of the current connection
     * @return the index of the next connection in the trip, or the first connection index if the current is last
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int nextConnectionId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer2.get(id);
    }

    /**
     * Returns the number of connections contained in the flattened connection data.
     *
     * @return the number of connections
     */
    @Override
    public int size() {
        return structuredBuffer1.size();
    }
}
