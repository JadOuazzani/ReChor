package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import java.util.Objects;
import static java.util.Objects.requireNonNull;

/**
 * Represents a stop with a name, a platform, and geographic coordinates.
 *
 * @param name         the name of the stop, must not be null.
 * @param platformName the name of the platform.
 * @param longitude    the longitude of the stop, must be between -180 and 180.
 * @param latitude     the latitude of the stop, must be between -90 and 90.
 */
public record Stop(String name, String platformName, double longitude,
                   double latitude) {

    private static final double MAX_LATITUDE = 90.0;
    private static final double MAX_LONGITUDE = 180.0;

    /**
     * Constructs a {@code Stop} with validated attributes.
     *
     * @throws NullPointerException if {@code name} is null.
     * @throws IllegalArgumentException if {@code latitude} or {@code longitude} are
     *                                  out of allowed bounds.
     */
    public Stop {
        requireNonNull(name);
        Preconditions.checkArgument(Math.abs(latitude) <= MAX_LATITUDE);
        Preconditions.checkArgument(Math.abs(longitude) <= MAX_LONGITUDE);
    }
}
