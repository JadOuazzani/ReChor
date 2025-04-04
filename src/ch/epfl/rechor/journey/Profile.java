package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an immutable profile corresponding to a specific timetable.
 * <p>
 * A profile contains the following attributes:
 * <ul>
 *   <li><code>timeTable</code>: the timetable to which the profile corresponds;</li>
 *   <li><code>date</code>: the date for which the profile is computed;</li>
 *   <li><code>arrStationId</code>: the index of the arrival station associated with the profile;</li>
 *   <li><code>stationFront</code>: the Pareto frontiers for all stations, where the element at a given index
 *       is the Pareto frontier for the station with the same index.</li>
 * </ul>
 * <p>
 * The compact constructor of {@code Profile} copies the provided Pareto frontiers list to guarantee immutability.
 * </p>
 *
 * @param timeTable    the timetable corresponding to this profile.
 * @param date         the date corresponding to this profile.
 * @param arrStationId the index of the arrival station.
 * @param stationFront the list of Pareto frontiers for all stations.
 */
public record Profile(TimeTable timeTable, LocalDate date, int arrStationId, List<ParetoFront> stationFront) {

    /**
     * Compact constructor that ensures immutability by copying the station front list.
     *
     * @param timeTable    the timetable corresponding to this profile.
     * @param date         the date corresponding to this profile.
     * @param arrStationId the index of the arrival station.
     * @param stationFront the list of Pareto frontiers for all stations.
     */
    public Profile {
        stationFront = List.copyOf(stationFront);
    }

    /**
     * Returns the connections corresponding to this profile.
     * <p>
     * These connections are simply those of the timetable for the date associated with this profile.
     * </p>
     *
     * @return the connections for this profile.
     */
    public Connections connections() {
        return timeTable.connectionsFor(date);
    }

    /**
     * Returns the trips corresponding to this profile.
     * <p>
     * These trips are simply those of the timetable for the date associated with this profile.
     * </p>
     *
     * @return the trips for this profile.
     */
    public Trips trips(){
        return timeTable.tripsFor(date);
    }

    /**
     * Returns the Pareto frontier for the station at the specified index.
     *
     * @param stationId the index of the station.
     * @return the Pareto frontier for the specified station.
     * @throws IndexOutOfBoundsException if the stationId is negative or not less than the number of stations.
     */
    public ParetoFront forStation(int stationId) {
        if (stationId < 0 || stationId >= stationFront.size()) {
            throw new IndexOutOfBoundsException();
        }
        return stationFront.get(stationId);
    }

    /**
     * A builder for constructing a {@code Profile} instance.
     * <p>
     * This builder is intended for use during the computation of optimal journeys. It stores not only
     * the Pareto frontier builders for stations but also for trips. In other words, it represents an
     * enhanced profile in construction, which eventually builds a simple profile containing only the
     * station Pareto frontiers.
     * </p>
     */
    public static final class Builder {
        private final TimeTable timeTable;
        private final LocalDate date;
        private final int arrStationId;
        private final ParetoFront.Builder[] stationBuilders;
        private final ParetoFront.Builder[] tripBuilders;

        /**
         * Constructs a builder for a profile corresponding to the specified timetable, date, and arrival station.
         * <p>
         * The constructor stores the provided arguments for later use when building the profile, and it initializes
         * two arrays intended to hold the Pareto frontier builders for stations and trips. These arrays are initially
         * filled with null values.
         * </p>
         *
         * @param timeTable    the timetable corresponding to the profile.
         * @param date         the date for which the profile is computed.
         * @param arrStationId the index of the arrival station.
         */
        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            this.stationBuilders = new ParetoFront.Builder[timeTable.stations().size()];
            this.tripBuilders = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
        }

        /**
         * Returns the builder for the Pareto frontier of the station at the given index.
         * <p>
         * If no builder has been set previously for that station, this method returns {@code null}.
         * </p>
         *
         * @param stationId the index of the station.
         * @return the builder for the Pareto frontier of the specified station, or {@code null} if not set.
         * @throws IndexOutOfBoundsException if the stationId is negative or not less than the number of stations.
         */
        public ParetoFront.Builder forStation(int stationId) {
            if (stationId < 0 || stationId >= stationBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            return stationBuilders[stationId];
        }

        /**
         * Associates the given Pareto frontier builder with the station at the specified index.
         *
         * @param stationId the index of the station.
         * @param builder   the Pareto frontier builder to associate with the station.
         * @throws IndexOutOfBoundsException if the stationId is negative or not less than the number of stations.
         */
        public void setForStation(int stationId, ParetoFront.Builder builder){
            if (stationId < 0 || stationId >= stationBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            stationBuilders[stationId] = builder;
        }

        /**
         * Returns the builder for the Pareto frontier of the trip at the specified index.
         *
         * @param tripId the index of the trip.
         * @return the builder for the Pareto frontier of the specified trip.
         * @throws IndexOutOfBoundsException if the tripId is negative or not less than the number of trips.
         */
        public ParetoFront.Builder forTrip(int tripId){
            if (tripId < 0 || tripId >= tripBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            return tripBuilders[tripId];
        }

        /**
         * Associates the given Pareto frontier builder with the trip at the specified index.
         *
         * @param tripId  the index of the trip.
         * @param builder the Pareto frontier builder to associate with the trip.
         * @throws IndexOutOfBoundsException if the tripId is negative or not less than the number of trips.
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder){
            if (tripId < 0 || tripId >= tripBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            tripBuilders[tripId] = builder;
        }

        /**
         * Builds and returns the immutable {@code Profile}.
         * <p>
         * This method constructs the profile by converting the array of station Pareto frontier builders into a list
         * of Pareto frontiers. For any station for which no builder was set (i.e., the element is {@code null}),
         * the empty Pareto frontier is used.
         * </p>
         *
         * @return the constructed {@code Profile}.
         */
        public Profile build(){
            List<ParetoFront> stationFront = new ArrayList<>(stationBuilders.length);
            for (ParetoFront.Builder builder : stationBuilders) {
                if (builder == null) {
                    stationFront.add(ParetoFront.EMPTY);
                } else {
                    stationFront.add(builder.build());
                }
            }
            return new Profile(timeTable, date, arrStationId, stationFront);
        }
    }
}
