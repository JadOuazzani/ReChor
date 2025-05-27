package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;
import java.util.Arrays;

import static ch.epfl.rechor.journey.PackedCriteria.*;

/**
 * A route planner that computes the Pareto‐optimal profile of all journeys
 * from any origin station to a specified destination station on a given date.
 * <p>
 * Internally, this uses the Connection Scan Algorithm (CSA) over the day's
 * connections and a precomputed walking time cache to the destination. For each
 * connection, it builds a temporary Pareto front of possible continuations,
 * applies two optimizations to skip empty or fully dominated fronts, and then
 * updates both the per‐trip and per‐station Pareto fronts maintained in the
 * Profile.Builder.
 *
 * @param timeTable
 *        the underlying {@link TimeTable} providing access to connections,
 *        transfer walking times, and station metadata
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public record Router(TimeTable timeTable) {

    /**
     * Builds and returns the complete profile of optimal journeys reaching
     * the specified destination station on the given travel date.
     * <p>
     * Steps performed:
     * <ol>
     *   <li>Precompute an array of walking times from every station to the
     *       destination using {@linkplain Transfers transfers}.</li>
     *   <li>Iterate through all {@linkplain Connections connections} in
     *       chronological departure order (earliest first).</li>
     *   <li>For each connection:
     *     <ul>
     *       <li>Option 1: Walk directly from its arrival station to the
     *           destination if possible.</li>
     *       <li>Option 2: Stay on board and continue with the same trip if a
     *           later segment exists.</li>
     *       <li>Option 3: Change vehicles at its arrival station.</li>
     *     </ul>
     *   </li>
     *   <li>Skip updating when no feasible continuation exists
     *       (optimization 1).</li>
     *   <li>Update the Pareto front for the trip corresponding to this
     *       connection.</li>
     *   <li>For each station from which one can walk to the connection's
     *       departure station, update its Pareto front with all newly
     *       discovered journeys (optimization 2 avoids redundant updates).</li>
     * </ol>
     *
     * @param date
     *        the {@link LocalDate} on which to compute journeys
     * @param destinationId
     *        the integer ID of the station to reach
     * @return a fully built {@link Profile} containing Pareto fronts of
     *         optimal journeys for every origin station and trip
     */
    public Profile profile(LocalDate date, int destinationId) {
        Profile.Builder profile = new Profile.Builder(timeTable, date, destinationId);
        Transfers transfers = timeTable.transfers();
        Connections connections = timeTable.connectionsFor(date);

        // Precompute walking times from every station to the destination
        int[] walkTimes = new int[timeTable.stations().size()];
        Arrays.fill(walkTimes, -1);
        int arrivalInterval = transfers.arrivingAt(destinationId);
        for (int i = PackedRange.startInclusive(arrivalInterval);
             i < PackedRange.endExclusive(arrivalInterval); i++) {
            walkTimes[transfers.depStationId(i)] = transfers.minutes(i);
        }

        // Process each connection in order
        for (int connIdx = 0; connIdx < connections.size(); connIdx++) {
            ParetoFront.Builder connFront = new ParetoFront.Builder();
            int arrStation = timeTable.stationId(connections.arrStopId(connIdx));
            int arrTime    = connections.arrMins(connIdx);
            int tripId     = connections.tripId(connIdx);

            // Option 1: walk to destination
            if (walkTimes[arrStation] != -1) {
                connFront.add(arrTime + walkTimes[arrStation], 0, connIdx);
            }

            // Option 2: continue on same trip
            if (profile.forTrip(tripId) != null) {
                connFront.addAll(profile.forTrip(tripId));
            }

            // Option 3: change vehicles
            if (profile.forStation(arrStation) != null) {
                int finalConnIdx = connIdx;
                profile.forStation(arrStation).forEach(t -> {
                    if (depMins(t) >= arrTime) {
                        long tuple = PackedCriteria.withoutDepMins(
                                PackedCriteria.withAdditionalChange(
                                        PackedCriteria.withPayload(t, finalConnIdx)
                                )
                        );
                        connFront.add(tuple);
                    }
                });
            }

            // Optimization 1: skip if no possible journeys
            if (connFront.isEmpty()) {
                continue;
            }

            // Update trip‐specific Pareto front
            if (profile.forTrip(tripId) == null) {
                profile.setForTrip(tripId, connFront);
            } else {
                profile.forTrip(tripId).addAll(connFront);
            }

            // Update station‐specific Pareto fronts for all stations that
            // can walk to this connection's departure station
            int depStation     = timeTable.stationId(connections.depStopId(connIdx));
            int depTime        = connections.depMins(connIdx);
            int accessInterval = transfers.arrivingAt(depStation);

            for (int j = PackedRange.startInclusive(accessInterval);
                 j < PackedRange.endExclusive(accessInterval); j++) {
                int walkFromStation = transfers.depStationId(j);
                int adjustedTime     = depTime - transfers.minutes(j);

                ParetoFront.Builder stationFront = new ParetoFront.Builder();
                int finalConnIdx1 = connIdx;
                connFront.forEach(t -> {
                    int stops = connections.tripPos(payload(t))
                            - connections.tripPos(finalConnIdx1);
                    long updated = withDepMins(
                            withPayload(t, Bits32_24_8.pack(finalConnIdx1, stops)),
                            adjustedTime
                    );
                    stationFront.add(updated);
                });

                if (profile.forStation(walkFromStation) != null) {
                    // Optimization 2: skip if fully dominated
                    if (profile.forStation(walkFromStation).fullyDominates(connFront, depTime)) {
                        continue;
                    }
                    profile.forStation(walkFromStation).addAll(stationFront);
                } else {
                    profile.setForStation(walkFromStation, stationFront);
                }
            }
        }

        return profile.build();
    }

}
