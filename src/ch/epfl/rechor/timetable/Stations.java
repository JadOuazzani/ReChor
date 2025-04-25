package ch.epfl.rechor.timetable;

/**
 * The Stations interface represents indexed train stations.
 *
 * <p>This interface provides the following abstract methods:
 * <ul>
 *   <li>{@code String name(int id)} – returns the name of the station at the given index;</li>
 *   <li>{@code double longitude(int id)} – returns the longitude (in degrees) of the station at the given index;</li>
 *   <li>{@code double latitude(int id)} – returns the latitude (in degrees) of the station at the given index.</li>
 * </ul>
 *
 * <p>All these methods throw an {@link IndexOutOfBoundsException} if the provided index
 * (referred to as {@code id}) is invalid, i.e., if it is less than 0 or greater than or equal to the size
 * returned by {@link #size()}.
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public interface Stations extends Indexed {
    /**
     * Returns the name of the station at the specified index.
     *
     * @param id the index of the station
     * @return the name of the station
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    String name(int id);

    /**
     * Returns the longitude, in degrees, of the station at the specified index.
     *
     * @param id the index of the station
     * @return the longitude (in degrees) of the station
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    double longitude(int id);

    /**
     * Returns the latitude, in degrees, of the station at the specified index.
     *
     * @param id the index of the station
     * @return the latitude (in degrees) of the station
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    double latitude(int id);
}
