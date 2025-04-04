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
 * @param legs the legs of the journey.
 */
public record Journey(List<Leg> legs) {

    /**
     * Constructs a {@code Journey} while verifying the provided list of legs.
     * <p>
     * The following conditions are checked:
     * <ul>
     *   <li>The list of legs must not be empty.</li>
     *   <li>Consecutive legs must alternate between walking and public transport.</li>
     *   <li>The departure time of each leg must not be before the arrival time of the previous leg.</li>
     *   <li>The departure stop of each leg must be equal to the arrival stop of the previous leg.</li>
     * </ul>
     * </p>
     *
     * @throws IllegalArgumentException if the list of legs is empty or if the legs are not properly connected.
     */
    public Journey {
        // Ensure the list is not null and not empty.
        Objects.requireNonNull(legs);
        Preconditions.checkArgument(!legs.isEmpty());
        legs = List.copyOf(legs); // for immutability

        // Verify that walking legs alternate with public transport legs,
        // that the departure time of each leg is not before the arrival time of the previous leg,
        // and that the departure stop matches the previous leg's arrival stop.
        for (int i = 1; i < legs.size(); i++) {
            boolean isCurrentFoot = legs.get(i) instanceof Journey.Leg.Foot;
            boolean isPreviousFoot = legs.get(i - 1) instanceof Journey.Leg.Foot;
            Preconditions.checkArgument(isCurrentFoot != isPreviousFoot);
            Preconditions.checkArgument(!legs.get(i).depTime().isBefore(legs.get(i - 1).arrTime())); // Departure time must not precede the previous leg's arrival time.
            Preconditions.checkArgument(legs.get(i).depStop().equals(legs.get(i - 1).arrStop())); // Departure stop must equal the previous leg's arrival stop.
        }
    }

    /**
     * Returns the departure stop of the journey.
     *
     * @return the departure stop of the journey.
     */
    public Stop depStop() {
        return legs.get(0).depStop();
    }

    /**
     * Returns the arrival stop of the journey.
     *
     * @return the arrival stop of the journey.
     */
    public Stop arrStop() {
        return legs.get(legs.size() - 1).arrStop();
    }

    /**
     * Returns the departure time of the journey.
     *
     * @return the departure time of the journey.
     */
    public LocalDateTime depTime() {
        return legs.get(0).depTime();
    }

    /**
     * Returns the arrival time of the journey.
     *
     * @return the arrival time of the journey.
     */
    public LocalDateTime arrTime() {
        return legs.get(legs.size() - 1).arrTime();
    }

    /**
     * Returns the total duration of the journey.
     *
     * @return the duration of the journey.
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
         * @return the departure stop of the leg.
         */
        Stop depStop();

        /**
         * Returns the departure time of the leg.
         *
         * @return the departure time of the leg.
         */
        LocalDateTime depTime();

        /**
         * Returns the arrival stop of the leg.
         *
         * @return the arrival stop of the leg.
         */
        Stop arrStop();

        /**
         * Returns the arrival time of the leg.
         *
         * @return the arrival time of the leg.
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
         * @return the duration of the leg.
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
        record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            /**
             * Constructs an {@code IntermediateStop} while verifying the consistency of the timings.
             *
             * @throws NullPointerException     if {@code stop} is null.
             * @throws IllegalArgumentException if {@code depTime} is before {@code arrTime}.
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
        record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime,
                         List<IntermediateStop> intermediateStops, Vehicle vehicle, String route,
                         String destination) implements Leg {

            /**
             * Constructs a {@code Transport} leg while verifying the validity of the parameters.
             *
             * @throws NullPointerException     if any parameter is null.
             * @throws IllegalArgumentException if {@code arrTime} is before {@code depTime}.
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
        record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg {

            /**
             * Constructs a {@code Foot} leg while verifying that none of the parameters are null and that
             * the arrival time is not before the departure time.
             *
             * @throws NullPointerException     if any parameter is null.
             * @throws IllegalArgumentException if {@code arrTime} is before {@code depTime}.
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
             * @return {@code true} if it is a transfer, {@code false} otherwise.
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }
}
