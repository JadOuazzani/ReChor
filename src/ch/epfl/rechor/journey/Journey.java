package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;

/**
 * Represents a journey.
 *
 * <p>A journey is composed of several legs. A journey's legs must be
 * non-empty and connected: walking and public transport legs must alternate,
 * each leg's departure time is not before the previous leg's arrival time, and
 * consecutive legs have matching stops.</p>
 *
 * @param legs the legs of the journey.
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public record Journey(List<Leg> legs) {

    private static final int FIRST_LEG_INDEX = 0;
    private static final int INITIAL_LOOP_INDEX = 1;
    private static final int LAST_LEG_OFFSET = 1;

    /**
     * Constructs a {@code Journey} while verifying the provided list of legs.
     * <p>
     * The following conditions are verified:
     * <ul>
     *   <li>The list of legs must not be empty.</li>
     *   <li>Consecutive legs must alternate between walking and public transport.</li>
     *   <li>The departure time of each leg must not be before the arrival time of the previous leg.</li>
     *   <li>The departure stop of each leg must equal the previous leg's arrival stop.</li>
     * </ul>
     * </p>
     */
    public Journey {
        Objects.requireNonNull(legs);
        Preconditions.checkArgument(!legs.isEmpty());
        legs = List.copyOf(legs);
        for (int i = INITIAL_LOOP_INDEX; i < legs.size(); i++) {
            Leg currentLeg = legs.get(i);
            Leg previousLeg = legs.get(i - 1);
            boolean currentIsFoot = currentLeg instanceof Leg.Foot;
            boolean previousIsFoot = previousLeg instanceof Leg.Foot;
            Preconditions.checkArgument(currentIsFoot != previousIsFoot);
            Preconditions.checkArgument(
                    !currentLeg.depTime().isBefore(previousLeg.arrTime()));
            Preconditions.checkArgument(
                    currentLeg.depStop().equals(previousLeg.arrStop()));
        }
    }

    /**
     * Returns the departure stop of the journey.
     *
     * @return the journey's departure stop.
     */
    public Stop depStop() {
        return legs.get(FIRST_LEG_INDEX).depStop();
    }

    /**
     * Returns the arrival stop of the journey.
     *
     * @return the journey's arrival stop.
     */
    public Stop arrStop() {
        return legs.get(legs.size() - LAST_LEG_OFFSET).arrStop();
    }

    /**
     * Returns the departure time of the journey.
     *
     * @return the journey's departure time.
     */
    public LocalDateTime depTime() {
        return legs.get(FIRST_LEG_INDEX).depTime();
    }

    /**
     * Returns the arrival time of the journey.
     *
     * @return the journey's arrival time.
     */
    public LocalDateTime arrTime() {
        return legs.get(legs.size() - LAST_LEG_OFFSET).arrTime();
    }

    /**
     * Returns the total duration of the journey.
     *
     * @return the journey's duration.
     */
    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }

    /**
     * Sealed interface representing a leg of a journey.
     */
    public sealed interface Leg {

        /**
         * Returns the departure stop of the leg.
         *
         * @return the leg's departure stop.
         */
        Stop depStop();

        /**
         * Returns the departure time of the leg.
         *
         * @return the leg's departure time.
         */
        LocalDateTime depTime();

        /**
         * Returns the arrival stop of the leg.
         *
         * @return the leg's arrival stop.
         */
        Stop arrStop();

        /**
         * Returns the arrival time of the leg.
         *
         * @return the leg's arrival time.
         */
        LocalDateTime arrTime();

        /**
         * Returns the list of intermediate stops of the leg.
         *
         * @return the list of intermediate stops.
         */
        List<IntermediateStop> intermediateStops();

        /**
         * Returns the total duration of the leg.
         *
         * @return the leg's duration.
         */
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * Represents an intermediate stop of a leg.
         *
         * @param stop    the intermediate stop.
         * @param arrTime the arrival time at the stop.
         * @param depTime the departure time from the stop.
         */
        record IntermediateStop(Stop stop, LocalDateTime arrTime,
                                LocalDateTime depTime) {

            /**
             * Constructs an {@code IntermediateStop} while verifying the consistency of
             * the timings.
             */
            public IntermediateStop {
                requireNonNull(stop);
                Preconditions.checkArgument(!depTime.isBefore(arrTime));
            }
        }

        /**
         * Represents a leg performed by public transport.
         *
         * @param vehicle     the type of vehicle used.
         * @param route       the name of the route on which the vehicle operates.
         * @param destination the final destination of the vehicle.
         */
        record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop,
                         LocalDateTime arrTime,
                         List<IntermediateStop> intermediateStops,
                         Vehicle vehicle, String route, String destination)
                implements Leg {

            /**
             * Constructs a {@code Transport} leg while verifying the validity of the
             * parameters.
             */
            public Transport {
                requireNonNull(vehicle);
                requireNonNull(route);
                requireNonNull(destination);
                requireNonNull(depStop);
                requireNonNull(depTime);
                requireNonNull(arrStop);
                requireNonNull(arrTime);
                intermediateStops = List.copyOf(intermediateStops);
                Preconditions.checkArgument(!arrTime.isBefore(depTime));
            }
        }

        /**
         * Represents a leg performed on foot.
         *
         * @param depStop the departure stop.
         * @param depTime the departure time.
         * @param arrStop the arrival stop.
         * @param arrTime the arrival time.
         */
        record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop,
                    LocalDateTime arrTime) implements Leg {

            /**
             * Constructs a {@code Foot} leg while verifying that none of the parameters are
             * null and that the arrival time is not before the departure time.
             */
            public Foot {
                requireNonNull(depStop);
                requireNonNull(depTime);
                requireNonNull(arrStop);
                requireNonNull(arrTime);
                Preconditions.checkArgument(!arrTime.isBefore(depTime));
            }

            /**
             * Returns an empty list since a walking leg has no intermediate stops.
             *
             * @return an empty list.
             */
            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            /**
             * Checks whether the leg represents a transfer within the same stop.
             *
             * @return {@code true} if it is a transfer; {@code false} otherwise.
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }
}
