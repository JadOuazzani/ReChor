package ch.epfl.rechor.timetable;

/**
 * The Platforms represents indexed platforms.
 *
 * <p>This interface provides the following abstract methods:
 * <ul>
 *   <li>{@code String name(int id)} – returns the name of the platform (e.g., "70" or "A"), which may be empty;</li>
 *   <li>{@code int stationId(int id)} – returns the index of the station to which this platform belongs.</li>
 * </ul>
 *
 * <p>All these methods throw an {@link IndexOutOfBoundsException} if the provided index is invalid,
 * i.e., if it is less than 0 or greater than or equal to the size returned by {@link #size()}.
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public interface Platforms extends Indexed {

    /**
     * Returns the name of the platform at the specified index.
     *
     * @param id the index of the platform
     * @return the name of the platform (e.g., "70" or "A"), which may be empty
     */
    String name(int id);

    /**
     * Returns the index of the station to which the platform at the specified index belongs.
     *
     * @param id the index of the platform
     * @return the station index corresponding to the platform
     */
    int stationId(int id);
}
