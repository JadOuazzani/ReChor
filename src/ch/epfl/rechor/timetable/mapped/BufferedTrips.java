package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U8;

/**
 * BufferedTrips is a final class that implements the {@link Trips} interface.
 * <p>
 * It provides access to a flattened table of trips (public transport runs), where the trip data
 * is stored in a {@link ByteBuffer} and the corresponding string values are resolved using a provided
 * string table. The flattened data is structured according to a {@link Structure} instance defined with two fields:
 * <ul>
 *   <li>{@code ROUTE_ID} (of type {@code U16}) representing the index of the route to which the trip belongs,</li>
 *   <li>{@code DESTINATION_ID} (of type {@code U16}) representing the index in the string table for
 the trip's final destination.</li>
 * </ul>
 * </p>
 * <p>
 * The route ID is retrieved from the structured buffer as an unsigned 16-bit value, and the destination is resolved
 * from the string table using the unsigned 16-bit value.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class BufferedTrips implements Trips {

    private final int ROUTE_ID = 0;
    private final int DESTINATION_ID = 1;

    private List<String> stringTable;
    private StructuredBuffer structuredBuffer;

    /**
     * Constructs a new BufferedTrips instance.
     * <p>
     * The provided {@code ByteBuffer} contains the flattened trip data, whose structure is defined by two fields:
     * <ul>
     *   <li>{@code ROUTE_ID} of type {@code U16} for the trip's route index, and</li>
     *   <li>{@code DESTINATION_ID} of type {@code U16} for the trip's destination string index.</li>
     * </ul>
     * The given string table is used to resolve the destination strings referenced by the flattened data.
     * </p>
     *
     * @param stringTable the list of strings used to resolve trip destination names
     * @param buffer      the ByteBuffer containing the flattened trip data
     */
    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        structuredBuffer = new StructuredBuffer(
                new Structure(
                        Structure.field(ROUTE_ID, U16),
                        Structure.field(DESTINATION_ID, U16)
                ),
                buffer
        );
    }

    /**
     * Returns the route ID for the trip at the specified index.
     * <p>
     * The route ID is stored as an unsigned 16-bit value in the flattened data.
     * </p>
     *
     * @param id the index of the trip
     * @return the route ID (i.e., the index of the route to which the trip belongs)
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int routeId(int id) throws IndexOutOfBoundsException {
        return structuredBuffer.getU16(ROUTE_ID, id);
    }

    /**
     * Returns the destination for the trip at the specified index.
     * <p>
     * The destination is retrieved from the flattened data as an unsigned 16-bit value, which is then used
     * as an index into the string table.
     * </p>
     *
     * @param id the index of the trip
     * @return the destination string of the trip
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public String destination(int id) throws IndexOutOfBoundsException {
        return stringTable.get(structuredBuffer.getU16(DESTINATION_ID, id));
    }

    /**
     * Returns the number of trips contained in the flattened data.
     *
     * @return the number of trips
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
