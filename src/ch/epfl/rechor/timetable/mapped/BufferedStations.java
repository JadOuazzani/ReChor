package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;
import java.nio.ByteBuffer;
import java.util.List;
import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;
import static java.lang.Math.scalb;

/**
 * BufferedStations is a final class that implements the {@link Stations} interface.
 * <p>
 * It provides access to a flattened table of stations, where the station data is stored in a
 * {@link ByteBuffer} and the string values are resolved using a provided string table.
 * The flattened data is structured according to a {@link Structure} instance, which in this case consists of:
 * <ul>
 *   <li>Field with index {@code NAME_ID} of type {@code U16} representing the station name's string index.</li>
 *   <li>Field with index {@code LON} of type {@code S32} representing the station's longitude.</li>
 *   <li>Field with index {@code LAG} of type {@code S32} representing the station's latitude.</li>
 * </ul>
 * The longitude and latitude values are scaled by a constant.
 * </p>
 */
public final class BufferedStations implements Stations {

    /** The field index for the station name. */
    public static final int NAME_ID = 0;
    /** The field index for the station longitude. */
    public static final int LON = 1;
    /** The field index for the station latitude. */
    public static final int LAG = 2;

    StructuredBuffer structuredBuffer;
    List<String> stringTable;
    private static final double CONSTANTE = scalb(360d, -32);

    /**
     * Constructs a new BufferedStations instance.
     * <p>
     * This constructor builds a {@code StructuredBuffer} using a {@link Structure} defined by three fields:
     * <ul>
     *   <li>{@code NAME_ID} (type {@code U16}) for the station name's string index,</li>
     *   <li>{@code LON} (type {@code S32}) for the station's longitude,</li>
     *   <li>{@code LAG} (type {@code S32}) for the station's latitude.</li>
     * </ul>
     * The given {@code ByteBuffer} contains the flattened station data and must have a capacity that is
     * a multiple of the total size of the structure.
     * </p>
     *
     * @param stringTable a list of strings representing the string table used to resolve station names
     * @param buffer      the ByteBuffer containing the flattened station data
     */
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(
                new Structure(
                        Structure.field(NAME_ID, U16),
                        Structure.field(LON, S32),
                        Structure.field(LAG, S32)
                ),
                buffer
        );
    }

    /**
     * Returns the name of the station at the specified index.
     *
     * @param id the index of the station
     * @return the station name resolved from the string table
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public String name(int id) {
        return stringTable.get(structuredBuffer.getU16(NAME_ID, id));
    }

    /**
     * Returns the longitude of the station at the specified index.
     * <p>
     * The longitude is stored as an S32 value in the structured buffer and is scaled by a constant.
     * </p>
     *
     * @param id the index of the station
     * @return the station's longitude in degrees
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public double longitude(int id) {
        return structuredBuffer.getS32(LON, id) * CONSTANTE;
    }

    /**
     * Returns the latitude of the station at the specified index.
     * <p>
     * The latitude is stored as an S32 value in the structured buffer and is scaled by a constant.
     * </p>
     *
     * @param id the index of the station
     * @return the station's latitude in degrees
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public double latitude(int id) {
        return structuredBuffer.getS32(LAG, id) * CONSTANTE;
    }

    /**
     * Returns the number of stations represented in this buffer.
     *
     * @return the number of stations
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
