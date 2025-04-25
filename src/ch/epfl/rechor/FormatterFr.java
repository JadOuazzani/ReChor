package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Provides textual representations for various data types.
 *
 * <p>
 * This final class contains static methods to format different aspects of a public transportation journey,
 * including durations, times, platform names, walking legs, transport legs, and route destinations.
 * The class is non-instantiable.
 * </p>
 *
 * <p>
 * Available methods:
 * <ul>
 *   <li>{@link #formatDuration(Duration)}: Formats a duration into hours and minutes.</li>
 *   <li>{@link #formatTime(LocalDateTime)}: Formats a date/time to display the time.</li>
 *   <li>{@link #formatPlatformName(Stop)}: Formats the platform name of a stop.</li>
 *   <li>{@link #formatLeg(Journey.Leg.Foot)}: Formats a walking leg.</li>
 *   <li>{@link #formatLeg(Journey.Leg.Transport)}: Formats a transport leg.</li>
 *   <li>{@link #formatRouteDestination(Journey.Leg.Transport)}: Formats the line and direction of a transport leg.</li>
 * </ul>
 * </p>
 *
 * @author
 *   Riad Elhariry, Jad Chahed Ouazzani
 */
public final class FormatterFr {

    private static final String MIN = " min";
    private static final String HOUR_SEPARATOR = " h ";

    /**
     * Private constructor to prevent instantiation.
     */
    private FormatterFr() {
        throw new UnsupportedOperationException("FormatterFr should not be instantiated.");
    }

    /**
     * Returns the formatted representation of a duration.
     *
     * <p>
     * If the duration is less than one hour, only the minutes are displayed.
     * Otherwise, the duration is displayed in hours and minutes.
     * </p>
     *
     * @param duration the duration to format; must not be null
     * @return the formatted duration string
     * @throws NullPointerException if duration is null
     */
    public static String formatDuration(Duration duration) {
        long totalSeconds = duration.getSeconds();
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours = (int) (totalSeconds / 3600);
        return hours == 0 ? minutes + MIN : hours + HOUR_SEPARATOR + minutes + MIN;
    }

    /**
     * Returns the formatted time from a LocalDateTime instance.
     *
     * <p>
     * The time is displayed using the format "Hhmm", where minutes are always shown as two digits.
     * </p>
     *
     * @param dateTime the LocalDateTime to format; must not be null
     * @return the formatted time string
     * @throws NullPointerException if dateTime is null
     */
    public static String formatTime(LocalDateTime dateTime) {
        int hours = dateTime.getHour();
        int minutes = dateTime.getMinute();
        return String.format("%dh%02d", hours, minutes);
    }

    /**
     * Returns the formatted platform name of a stop.
     *
     * <p>
     * Railway platforms are identified by numbers, while bus station platforms are identified by letters.
     * If the stop corresponds to a bus station or the platform name is empty, an empty string is returned.
     * </p>
     *
     * @param stop the stop whose platform is to be formatted; must not be null
     * @return the formatted platform string, or an empty string if not available
     * @throws NullPointerException if stop is null
     */
    public static String formatPlatformName(Stop stop) {
        String platformName = stop.platformName();
        if (platformName == null || platformName.isEmpty()) {
            return "";
        }
        return Character.isDigit(platformName.charAt(0))
                ? "voie " + platformName
                : "quai " + platformName;
    }

    /**
     * Returns the formatted representation of a walking leg.
     *
     * <p>
     * The leg is described by its type ("changement" for transfers or "trajet à pied" for a normal walk)
     * followed by its duration in parentheses.
     * </p>
     *
     * @param walkingLeg the walking leg to format; must not be null
     * @return the formatted walking leg string
     * @throws NullPointerException if walkingLeg is null
     */
    public static String formatLeg(Journey.Leg.Foot walkingLeg) {
        String legDuration = formatDuration(walkingLeg.duration());
        String prefix = walkingLeg.isTransfer() ? "changement " : "trajet à pied ";
        return prefix + "(" + legDuration + ")";
    }

    /**
     * Returns the formatted representation of a transport leg.
     *
     * <p>
     * The leg is described by the departure time and stop (including platform information if available),
     * followed by an arrow, the arrival stop (with platform information if available), and the arrival time.
     * </p>
     *
     * @param transportLeg the transport leg to format; must not be null
     * @return the formatted transport leg string
     * @throws NullPointerException if transportLeg is null
     */
    public static String formatLeg(Journey.Leg.Transport transportLeg) {
        String departurePlatform = transportLeg.depStop().platformName() == null
                ? ""
                : " (" + formatPlatformName(transportLeg.depStop()) + ")";
        String arrivalPlatform = transportLeg.arrStop().platformName() == null
                ? ")"
                : " " + formatPlatformName(transportLeg.arrStop()) + ")";
        return new StringBuilder()
                .append(formatTime(transportLeg.depTime()))
                .append(" ")
                .append(transportLeg.depStop().name())
                .append(departurePlatform)
                .append(" → ")
                .append(transportLeg.arrStop().name())
                .append(" (arr. ")
                .append(formatTime(transportLeg.arrTime()))
                .append(arrivalPlatform)
                .toString();
    }

    /**
     * Returns the formatted route and destination of the vehicle used for a transport leg.
     *
     * <p>
     * The route and destination are concatenated to form the description of the transport.
     * </p>
     *
     * @param transportLeg the transport leg from which to extract the route and destination; must not be null
     * @return the formatted route and destination string
     * @throws NullPointerException if transportLeg is null
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        return transportLeg.route() + " Direction " + transportLeg.destination();
    }
}
