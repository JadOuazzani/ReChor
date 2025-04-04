package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;
import java.nio.ByteBuffer;
import java.util.List;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U8;

/**
 * BufferedRoutes is a final class that implements the {@link Routes} interface.
 * <p>
 * It provides access to a flattened table of public transport routes, where the route data
 * is stored in a {@link ByteBuffer} and the associated string values are resolved using a provided
 * string table. The flattened data is structured by a {@link Structure} instance defined with two fields:
 * <ul>
 *   <li>{@code NAME_ID} of type {@code U16} representing the route name (e.g., "IR 15"),</li>
 *   <li>{@code KIND} of type {@code U8} representing the index of the vehicle type serving the route.</li>
 * </ul>
 * The vehicle type is determined by mapping the unsigned 8-bit value to a {@link Vehicle} using
 * {@code Vehicle.values()}.
 * </p>
 */
public final class BufferedRoutes implements Routes {

    private final int NAME_ID = 0;
    private final int KIND = 1;

    private List<String> stringTable;
    private StructuredBuffer structuredBuffer;

    /**
     * Constructs a new BufferedRoutes instance.
     * <p>
     * The provided {@code ByteBuffer} contains the flattened route data and must have a capacity
     * that is a multiple of the total size of the defined structure. The given string table is used
     * to resolve the string values referenced by the flattened data.
     * The structure is defined with two fields:
     * <ul>
     *   <li>{@code NAME_ID} of type {@code U16} for the route name,</li>
     *   <li>{@code KIND} of type {@code U8} for the vehicle type index.</li>
     * </ul>
     * </p>
     *
     * @param stringTable the list of strings used to resolve route names
     * @param buffer      the ByteBuffer containing the flattened route data
     */
    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(
                new Structure(
                        Structure.field(NAME_ID, U16),
                        Structure.field(KIND, U8)
                ),
                buffer
        );
    }

    /**
     * Returns the {@link Vehicle} type for the route at the specified index.
     * <p>
     * The vehicle type is determined by reading an unsigned 8-bit value from the flattened data,
     * and then mapping it to a {@code Vehicle} using {@code Vehicle.values()}.
     * </p>
     *
     * @param id the index of the route
     * @return the vehicle type serving the route
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public Vehicle vehicle(int id) throws IndexOutOfBoundsException {
        int vehicleId = structuredBuffer.getU8(KIND, id);
        return Vehicle.values()[vehicleId];
    }

    /**
     * Returns the name of the route at the specified index.
     * <p>
     * The route name is obtained by reading an unsigned 16-bit value from the flattened data,
     * which is then used as an index in the string table.
     * </p>
     *
     * @param id the index of the route
     * @return the route name (e.g., "IR 15")
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public String name(int id) throws IndexOutOfBoundsException {
        return stringTable.get(structuredBuffer.getU16(NAME_ID, id));
    }

    /**
     * Returns the number of routes contained in the flattened data.
     *
     * @return the number of routes
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
