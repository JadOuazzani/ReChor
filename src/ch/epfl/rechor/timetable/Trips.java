package ch.epfl.rechor.timetable;

/**
 * Trips represents indexed public transport trips.
 *
 * <p>This interface provides the following abstract methods:
 * <ul>
 *   <li>{@code int routeId(int id)} – returns the index of the route (line) to which the trip at the given
 index belongs;</li>
 *   <li>{@code String destination(int id)} – returns the final destination name of the trip at the given index.</li>
 * </ul>
 *
 * <p>All methods throw an {@link IndexOutOfBoundsException} if the provided index (referred to as {@code id})
 * is invalid, i.e., if it is less than 0 or greater than or equal to the value returned by {@link Indexed#size()}.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public interface Trips extends Indexed {

    /**
     * Returns the index of the route (line) for the trip at the specified index.
     *
     * @param id the index of the trip
     * @return the index of the route to which the trip belongs
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@link #size()}
     */
    int routeId(int id);

    /**
     * Returns the final destination name of the trip at the specified index.
     *
     * @param id the index of the trip
     * @return the destination name of the trip
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@link #size()}
     */
    String destination(int id);
}
