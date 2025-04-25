package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;

/**
 * BufferedPlatforms is a final class that implements the {@link Platforms} interface.
 * <p>
 * It provides access to a flattened table of platforms, where the platform data is stored in a
 * {@link ByteBuffer} and the corresponding string values are resolved using a provided string table.
 * The flattened data is structured by a {@link Structure} instance that defines two fields:
 * <ul>
 *   <li>{@code NAME_ID} of type {@code U16} representing the platform name, and</li>
 *   <li>{@code STATION_ID} of type {@code U16} representing the index of the station to which the platform
 belongs.</li>
 * </ul>
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class BufferedPlatforms implements Platforms {
    private final int NAME_ID = 0;
    private final int STATION_ID = 1;
    private List<String> stringTable;
    private StructuredBuffer structuredBuffer;

    /**
     * Constructs a new BufferedPlatforms instance.
     * <p>
     * The given {@code ByteBuffer} contains the flattened platform data, and the provided string table is used
     * to resolve the string values referenced by this data.
     * The structure of the data is defined by two fields:
     * <ul>
     *   <li>{@code NAME_ID} of type {@code U16} for the platform name,</li>
     *   <li>{@code STATION_ID} of type {@code U16} for the station index associated with the platform.</li>
     * </ul>
     * </p>
     *
     * @param stringTable the list of strings used to resolve platform names
     * @param buffer      the ByteBuffer containing the flattened platform data
     */
    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        structuredBuffer = new StructuredBuffer(
                new Structure(
                        Structure.field(NAME_ID, U16),
                        Structure.field(STATION_ID, U16)
                ),
                buffer
        );
    }

    /**
     * Returns the name of the platform at the specified index.
     *
     * @param id the index of the platform
     * @return the platform name, resolved from the string table
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public String name(int id) {
        return stringTable.get(structuredBuffer.getU16(NAME_ID, id));
    }

    /**
     * Returns the index of the station to which the platform at the specified index belongs.
     *
     * @param id the index of the platform
     * @return the station index corresponding to the platform
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public int stationId(int id) {
        return structuredBuffer.getU16(STATION_ID, id);
    }

    /**
     * Returns the number of platforms represented in the flattened data.
     *
     * @return the number of platforms
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
