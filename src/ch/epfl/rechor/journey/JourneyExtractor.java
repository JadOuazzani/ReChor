package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.*;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A class that extracts journeys from a profile.
 */
public final class JourneyExtractor {
    // Prevent instantiation
    private JourneyExtractor() {
    }

    /**
     * Returns all optimal journeys for the given profile and departure station.
     *
     * @param profile      the profile containing journey information
     * @param depStationId the ID of the departure station
     * @return a list of optimal journeys sorted by departure time then arrival time
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        ParetoFront front = profile.forStation(depStationId);

        // Iterate through each journey option in the Pareto front
        front.forEach(packedCriteria -> {

            // Extract journey information from the criteria
            List<Journey.Leg> legs = extractLegs(profile, depStationId, packedCriteria);

            journeys.add(new Journey(legs));

        });

        // Sort by departure time, then by arrival time
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Extracts all legs for a journey based on the packed criteria.
     */
    private static List<Journey.Leg> extractLegs(Profile profile, int stationId, long packedCriteria) {
        List<Journey.Leg> legs = new ArrayList<>();
        TimeTable timeTable = profile.timeTable();
        Connections connections = profile.connections();
        Trips trips = profile.trips();
        Routes routes = timeTable.routes();

        // Current state for journey extraction
        int currentStationId = timeTable.stationId(stationId);
        int plateformeStationId = timeTable.stationId(profile.arrStationId());
        int remainingChanges = PackedCriteria.changes(packedCriteria);
        int targetArrMinutes = PackedCriteria.arrMins(packedCriteria);

        // Extract first leg payload
        int firstConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(packedCriteria));
        int skipStops = Bits32_24_8.unpack8(PackedCriteria.payload(packedCriteria));

        // Add initial foot leg if needed
        int firstConnectionDepStopId = connections.depStopId(firstConnectionId);

        if (currentStationId != firstConnectionDepStopId) {
            // Need to add a foot leg from departure station to first connection station
            firstConnectionDepStopId = timeTable.stationId(firstConnectionDepStopId);

            int transferMinutes = timeTable.transfers().minutesBetween(currentStationId, firstConnectionDepStopId);

            Stop depStop = createStop(timeTable, currentStationId);
            Stop arrStop = createStop(timeTable, connections.depStopId(firstConnectionId));

            LocalDateTime depTime = timeToDateTime(PackedCriteria.depMins(packedCriteria), profile.date());
            LocalDateTime arrTime = depTime.plusMinutes(transferMinutes);

            Journey.Leg.Foot footLeg = new Journey.Leg.Foot(depStop, depTime, arrStop, arrTime);
            legs.add(footLeg);

        }

        // Process transport legs and transfers until we reach the destination
        int currentConnectionId = firstConnectionId;
        LocalDateTime arrTime = timeToDateTime(connections.arrMins(currentConnectionId), profile.date());

        while (remainingChanges != -1) {
            // Extract transport leg
            int tripId = connections.tripId(currentConnectionId);
            int routeId = trips.routeId(tripId);

            // Create stops
            int depStopId = connections.depStopId(currentConnectionId);
            int arrStopId = connections.depStopId(currentConnectionId);
            List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
            for (int i = 0; i < skipStops; i++) {
                currentConnectionId = connections.nextConnectionId(currentConnectionId);
                arrStopId = connections.arrStopId(currentConnectionId);
                Stop intermediateStop = createStop(timeTable, connections.arrStopId(currentConnectionId));
                LocalDateTime intermediateArrTime = timeToDateTime(
                        connections.arrMins(currentConnectionId), profile.date());
                LocalDateTime intermediateDepTime = timeToDateTime(
                        connections.depMins(currentConnectionId), profile.date());

                intermediateStops.add(new Journey.Leg.IntermediateStop(
                        intermediateStop, intermediateDepTime, intermediateArrTime));
            }

            Stop depStop = createStop(timeTable, depStopId);
            Stop arrStop = createStop(timeTable, arrStopId);

            // Get times for the transport leg
            LocalDateTime depTime = timeToDateTime(connections.depMins(currentConnectionId), profile.date());
            arrTime = timeToDateTime(connections.arrMins(currentConnectionId), profile.date());
            Vehicle vehicle = routes.vehicle(routeId);
            String routeName = routes.name(routeId);
            String destination = trips.destination(tripId);

            Journey.Leg.Transport transportLeg = new Journey.Leg.Transport(
                    depStop, depTime, arrStop, arrTime, intermediateStops,
                    vehicle, routeName, destination);

            legs.add(transportLeg);



            int arrStationId = connections.arrStopId(currentConnectionId);

            //Get arrival station ID
            if (!timeTable.isStationId(arrStationId)) {
                arrStationId = timeTable.stationId(arrStationId);
            }


            // Look up the next leg from the arrival station's Pareto front
            ParetoFront arrStationFront = profile.forStation(arrStationId);

            // We need to find the tuple with remainingChanges-1 changes and our target arrival time
            long nextPackedCriteria;
            if (remainingChanges != 0) {
                nextPackedCriteria = arrStationFront.get(targetArrMinutes, remainingChanges - 1);
                // Extract next connection information
                int nextConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(nextPackedCriteria));
                skipStops = Bits32_24_8.unpack8(PackedCriteria.payload(nextPackedCriteria));

                // Add foot leg for the transfer
                int nextConnectionStationId = connections.depStopId(nextConnectionId);

                if (!timeTable.isStationId(nextConnectionStationId)) {
                    nextConnectionStationId = timeTable.platforms().stationId(nextConnectionStationId - profile.timeTable().stations().size());
                }
                // Need to add a foot leg for transfer between stations
                int transferMinutes = timeTable.transfers().minutesBetween(arrStationId, nextConnectionStationId);

                Stop transferDepStop = createStop(timeTable, arrStopId);
                Stop transferArrStop = createStop(timeTable, connections.depStopId(nextConnectionId));

                LocalDateTime transferDepTime = arrTime;
                LocalDateTime transferArrTime = transferDepTime.plusMinutes(transferMinutes);

                Journey.Leg.Foot footLeg = new Journey.Leg.Foot(transferDepStop, transferDepTime, transferArrStop, transferArrTime);
                legs.add(footLeg);

                // Update for next iteration
                currentConnectionId = nextConnectionId;
            }
            remainingChanges--;
        }

        int arrivalIdStopId = connections.arrStopId(currentConnectionId);
        int arrStopId = timeTable.stationId(arrivalIdStopId);

        if (arrStopId != profile.arrStationId()) {
            // Need to add a foot leg for transfer between stations
            int transferMinutes = timeTable.transfers().minutesBetween(plateformeStationId, arrStopId);

            Stop transferDepStop = createStop(timeTable, connections.arrStopId(currentConnectionId));
            Stop transferArrStop = createStop(timeTable, plateformeStationId);

            LocalDateTime transferDepTime = arrTime;
            LocalDateTime transferArrTime = transferDepTime.plusMinutes(transferMinutes);

            Journey.Leg.Foot footLeg = new Journey.Leg.Foot(transferDepStop, transferDepTime, transferArrStop, transferArrTime);
            legs.add(footLeg);
        }

        return legs;
    }

    /**
     * Creates a Stop object from a stop ID (which may be a station or platform).
     */
    private static Stop createStop(TimeTable timeTable, int stopId) {
        Platforms platforms = timeTable.platforms();
        int stationId;
        if (timeTable.isPlatformId(stopId)) stationId = platforms.stationId(stopId - timeTable.stations().size());
        else stationId = stopId;
        Stations stations = timeTable.stations();
        return new Stop(stations.name(stationId), timeTable.platformName(stopId), stations.longitude(stationId), stations.latitude(stationId));
    }

    /**
     * Converts minutes since midnight to a LocalDateTime.
     */
    private static LocalDateTime timeToDateTime(int minutesSinceMidnight, LocalDate date) {
        return date.atStartOfDay().plusMinutes(minutesSinceMidnight);
    }


}
