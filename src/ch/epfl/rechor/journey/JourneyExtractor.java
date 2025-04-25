package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ch.epfl.rechor.Bits32_24_8.unpack24;
import static ch.epfl.rechor.Bits32_24_8.unpack8;
import static ch.epfl.rechor.journey.PackedCriteria.*;

/**
 * Extracts journeys from a profile based on departure station and criteria.
 * <p>
 * This class provides methods to extract and build journeys from a given {@link Profile}. Journeys are constructed
 * using connection criteria and sorted by departure and arrival times.
 * </p>
 *
 * <p>
 * Note: This class is not instantiable.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class JourneyExtractor {

    /**
     * Private constructor to prevent instantiation.
     */
    private JourneyExtractor() {
    }

    /**
     * Extracts a list of journeys from the given profile starting at the specified departure station.
     * <p>
     * For the specified departure station, the method retrieves all criteria and builds journeys accordingly.
     * The resulting journeys are then sorted by their departure times and, in case of ties, by their arrival times.
     * </p>
     *
     * @param profile      the profile containing timetable and connection data.
     * @param depStationId the ID of the departure station.
     * @return a list of journeys starting from the specified departure station.
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        profile.forStation(depStationId)
                .forEach(criteria -> journeys.add(buildJourney(profile, depStationId, criteria)));
        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));
        return journeys;
    }

    /**
     * Builds a single journey based on the given criteria.
     * <p>
     * This method extracts connection information from the profile and constructs the journey legs.
     * It processes the initial connection, intermediate transport and foot legs (for transfers), and adds a
     final foot leg
     * if the final arrival station differs from the profile's arrival station.
     * </p>
     *
     * @param profile      the profile containing timetable data.
     * @param depStationId the departure station ID.
     * @param criteria     the packed criteria used to determine the journey's connections and transfers.
     * @return the constructed {@link Journey}.
     */
    private static Journey buildJourney(Profile profile, int depStationId, long criteria) {
        TimeTable tt = profile.timeTable();
        Connections conn = profile.connections();
        LocalDate date = profile.date();
        List<Journey.Leg> legs = new ArrayList<>();

        // Extract initial connection info
        int firstConnId = unpack24(payload(criteria));
        int firstDepStopId = conn.depStopId(firstConnId);
        int firstArrStopId = conn.arrStopId(firstConnId);
        int firstDepStation = tt.stationId(firstDepStopId);
        int firstArrStation = tt.stationId(firstArrStopId);
        LocalDateTime arrivingTime = date.atStartOfDay().plusMinutes(conn.arrMins(firstConnId));

        // If the departure station from the connection does not match the specified departure station,
        // add an initial foot leg to connect them.
        if (firstDepStation != depStationId) {
            legs.add(new Journey.Leg.Foot(
                    createStop(tt, depStationId),
                    date.atStartOfDay().plusMinutes(depMins(criteria)).minusMinutes(1),
                    createStop(tt, firstDepStopId),
                    date.atStartOfDay().plusMinutes(depMins(criteria))
            ));
        }

        // Process all legs (both transfers and transport legs)
        int currentConnId = firstConnId;
        int intermediateStops = unpack8(payload(criteria));
        int changes = changes(criteria);
        Stop currentArrStop = createStop(tt, firstArrStopId);

        for (int changesLeft = changes; changesLeft >= 0; changesLeft--) {
            if (changesLeft != changes) {
                // Update criteria for the next leg if a transfer occurs.
                criteria = profile.forStation(firstArrStation).get(arrMins(criteria), changesLeft);
                currentConnId = unpack24(payload(criteria));
                intermediateStops = unpack8(payload(criteria));
                int depStopId = conn.depStopId(currentConnId);
                firstDepStation = tt.stationId(depStopId);

                legs.add(new Journey.Leg.Foot(
                        currentArrStop,
                        arrivingTime,
                        createStop(tt, depStopId),
                        arrivingTime.plusMinutes(tt.transfers().minutesBetween(firstArrStation, firstDepStation))
                ));
            }
            // Process the transport leg
            List<Journey.Leg.IntermediateStop> interStops = new ArrayList<>();
            int connToProcess = currentConnId;

            for (int i = 0; i < intermediateStops; i++) {
                int arrMins = conn.arrMins(connToProcess);
                connToProcess = conn.nextConnectionId(connToProcess);
                int stopId = conn.depStopId(connToProcess);
                int depMins = conn.depMins(connToProcess);

                interStops.add(new Journey.Leg.IntermediateStop(
                        createStop(tt, stopId),
                        date.atStartOfDay().plusMinutes(arrMins),
                        date.atStartOfDay().plusMinutes(depMins)
                ));
            }

            int tripId = conn.tripId(connToProcess);
            int arrStopId = conn.arrStopId(connToProcess);
            firstArrStation = tt.stationId(arrStopId);
            currentArrStop = createStop(tt, arrStopId);
            arrivingTime = date.atStartOfDay().plusMinutes(conn.arrMins(connToProcess));

            legs.add(new Journey.Leg.Transport(
                    createStop(tt, conn.depStopId(currentConnId)),
                    date.atStartOfDay().plusMinutes(conn.depMins(currentConnId)),
                    currentArrStop,
                    arrivingTime,
                    interStops,
                    tt.routes().vehicle(profile.trips().routeId(tripId)),
                    tt.routes().name(profile.trips().routeId(tripId)),
                    profile.trips().destination(tripId)
            ));
        }

        // Add a final foot leg if needed to connect to the profile's arrival station.
        if (firstArrStation != profile.arrStationId()) {
            legs.add(new Journey.Leg.Foot(
                    currentArrStop,
                    arrivingTime,
                    createStop(tt, profile.arrStationId()),
                    arrivingTime.plusMinutes(tt.transfers().minutesBetween(firstArrStation, profile.arrStationId()))
            ));
        }
        return new Journey(legs);
    }

    /**
     * Creates a {@link Stop} object based on the given stop ID.
     * <p>
     * The method retrieves the station ID corresponding to the stop ID and then constructs a new {@link Stop}
     * using the station name, platform name (if applicable), longitude, and latitude.
     * </p>
     *
     * @param tt     the timetable containing station and stop data.
     * @param stopId the ID of the stop.
     * @return a newly created {@link Stop}.
     */
    private static Stop createStop(TimeTable tt, int stopId) {
        int stationId = tt.stationId(stopId);
        return new Stop(
                tt.stations().name(stationId),
                tt.isPlatformId(stopId) ? tt.platformName(stopId) : "",
                tt.stations().longitude(stationId),
                tt.stations().latitude(stationId)
        );
    }

    /**
     * Creates a new foot leg for the journey.
     * <p>
     * A foot leg represents a walking segment between two stops. This method constructs a new walking leg
     * with the specified departure and arrival stops and times.
     * </p>
     *
     * @param currentArrStop the departure stop for the foot leg.
     * @param arrivingTime   the departure time for the foot leg.
     * @param nextStop       the arrival stop for the foot leg.
     * @param arrivingTimePlus the arrival time at the next stop.
     * @return a new walking leg represented as {@link Journey.Leg.Foot}.
     */
}
