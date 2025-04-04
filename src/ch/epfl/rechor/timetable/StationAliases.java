package ch.epfl.rechor.timetable;

/**
 * The StationAliases represents alternative names for stations.
 *
 * <p>This interface provides the following abstract methods:
 * <ul>
 *   <li>{@code String alias(int id)} – returns the alternative name at the given index (e.g., "Losanna");</li>
 *   <li>{@code String stationName(int id)} – returns the actual station name corresponding to the alternative name at the given index (e.g., "Lausanne").</li>
 * </ul>
 *
 * <p>All these methods throw an {@link IndexOutOfBoundsException} if the provided index is invalid,
 * meaning it is less than 0 or greater than or equal to the value returned by {@link #size()}.
 */
public interface StationAliases extends Indexed {

    /**
     * Returns the alternative name of the station at the specified index.
     *
     * @param id the index of the alternative name
     * @return the alternative name (for example, "Losanna")
     */
    String alias(int id);

    /**
     * Returns the actual station name corresponding to the alternative name at the specified index.
     *
     * @param id the index of the alternative name
     * @return the station name (for example, "Lausanne")
     * <p>
     */
    String stationName(int id);
}


