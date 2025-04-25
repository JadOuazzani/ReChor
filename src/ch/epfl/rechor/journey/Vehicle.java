package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Enumeration representing the different types of vehicles that can be used in a journey.
 *
 * <p>This enum provides the following vehicle types:
 * <ul>
 *   <li>{@code TRAM}: Tram.</li>
 *   <li>{@code METRO}: Metro.</li>
 *   <li>{@code TRAIN}: Train.</li>
 *   <li>{@code BUS}: Bus.</li>
 *   <li>{@code FERRY}: Ferry.</li>
 *   <li>{@code AERIAL_LIFT}: Aerial lift (e.g., cable car, gondola, etc.).</li>
 *   <li>{@code FUNICULAR}: Funicular.</li>
 * </ul>
 * </p>
 *
 * <p>An immutable list of all vehicles is available via the {@code ALL} constant.</p>
 *
 * @author
 *   Riad Elhariry, Jad Chahed Ouazzani
 */
public enum Vehicle {
    /**
     * Tram.
     */
    TRAM,
    /**
     * Metro.
     */
    METRO,
    /**
     * Train.
     */
    TRAIN,
    /**
     * Bus.
     */
    BUS,
    /**
     * Ferry.
     */
    FERRY,
    /**
     * Aerial lift (e.g., cable car, gondola, etc.).
     */
    AERIAL_LIFT,
    /**
     * Funicular.
     */
    FUNICULAR;

    /**
     * Immutable list containing all vehicle types.
     */
    public static final List<Vehicle> ALL = List.of(Vehicle.values());
}
