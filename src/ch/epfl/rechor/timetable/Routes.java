package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;

/**
 * Routes represents indexed public transport routes.
 *
 * <p>This interface provides the following abstract methods:
 * <ul>
 *   <li>{@code Vehicle vehicle(int id)} – returns the type of vehicle serving the route at the given index;</li>
 *   <li>{@code String name(int id)} – returns the name of the route at the given index (e.g., "IR 15").</li>
 * </ul>
 *
 * <p>All methods throw an {@link IndexOutOfBoundsException} if the provided index (referred to as {@code id})
 * is invalid, i.e., if it is less than 0 or greater than or equal to the value returned by {@link #size()}.
 */
public interface Routes extends Indexed {

    /**
     * Returns the type of vehicle serving the route at the specified index.
     *
     * @param id the index of the route
     * @return the vehicle type for the route (e.g., TRAIN, BUS, etc.)
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    Vehicle vehicle(int id);

    /**
     * Returns the name of the route at the specified index.
     *
     * @param id the index of the route
     * @return the name of the route (e.g., "IR 15")
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    String name(int id);
}
