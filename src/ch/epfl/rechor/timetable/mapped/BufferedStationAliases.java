package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.U16;

/**
 * BufferedStationAliases is a final class that implements the {@link StationAliases} interface.
 * <p>
 * It provides access to a flattened table of alternative station names, where the data is stored
 * in a {@link ByteBuffer} and the string values are resolved using a provided string table.
 * The flattened data is structured according to a {@link ch.epfl.rechor.timetable.mapped.Structure}
 * defined by two fields: one for the alias and one for the corresponding station name.
 * </p>
 *
 * <p>
 * For example, once an instance is constructed, you can obtain the alias at a given index as follows:
 * <pre>
 * BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);
 * String alias = aliases.alias(index);
 * </pre>
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class BufferedStationAliases implements StationAliases {

    private final int ALIAS_ID = 0;
    private final int STATION_NAME_ID = 1;

    private List<String> stringTable;
    private StructuredBuffer structuredBuffer;

    /**
     * Constructs a new BufferedStationAliases instance.
     * <p>
     * This constructor creates a {@link StructuredBuffer} using a {@link ch.epfl.rechor.timetable.mapped.Structure}
     * defined with two fields:
     * <ul>
     *   <li>{@code ALIAS_ID} of type {@code U16}, representing the alias string index, and</li>
     *   <li>{@code STATION_NAME_ID} of type {@code U16}, representing the station name string index.</li>
     * </ul>
     * The provided {@code ByteBuffer} must contain the flattened data corresponding to these fields,
     * and the string table is used to resolve the actual strings.
     * </p>
     *
     * @param stringTable the list of strings used to resolve the aliases and station names
     * @param buffer      the ByteBuffer containing the flattened data
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        structuredBuffer = new StructuredBuffer(
                new ch.epfl.rechor.timetable.mapped.Structure(
                        ch.epfl.rechor.timetable.mapped.Structure.field(ALIAS_ID, U16),
                        ch.epfl.rechor.timetable.mapped.Structure.field(STATION_NAME_ID, U16)
                ),
                buffer
        );
    }

    /**
     * Returns the alias of the station at the specified index.
     *
     * @param id the index of the alias
     * @return the alias string
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public String alias(int id) {
        return stringTable.get(structuredBuffer.getU16(ALIAS_ID, id));
    }

    /**
     * Returns the corresponding station name for the alias at the specified index.
     *
     * @param id the index of the alias
     * @return the station name string associated with the alias
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public String stationName(int id) {
        return stringTable.get(structuredBuffer.getU16(STATION_NAME_ID, id));
    }

    /**
     * Returns the number of alternative station name entries.
     *
     * @return the size of the flattened station aliases table
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
