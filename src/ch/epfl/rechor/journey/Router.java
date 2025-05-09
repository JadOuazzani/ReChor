package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;
import java.util.Arrays;

import static ch.epfl.rechor.journey.PackedCriteria.*;

public record Router(TimeTable timeTable) {

    public Profile profile(LocalDate date, int destinationId) {
        Profile.Builder profile = new Profile.Builder(timeTable, date, destinationId);
        Transfers transfers = timeTable.transfers();
        Connections connections = timeTable.connectionsFor(date);

        // Pré-calcul des temps de marche vers la destination
        int[] walkTimes = new int[timeTable.stations().size()];
        Arrays.fill(walkTimes, -1);
        int arrivalInterval = transfers.arrivingAt(destinationId);
        for (int i = PackedRange.startInclusive(arrivalInterval); i < PackedRange.endExclusive(arrivalInterval); i++) {
            walkTimes[transfers.depStationId(i)] = transfers.minutes(i);
        }

        // Traitement des connexions
        for (int connIdx = 0; connIdx < connections.size(); connIdx++) {
            ParetoFront.Builder connFront = new ParetoFront.Builder();
            int arrStation = timeTable.stationId(connections.arrStopId(connIdx));
            int arrTime = connections.arrMins(connIdx);
            int tripId = connections.tripId(connIdx);

            // Option 1: Marcher à destination
            if (walkTimes[arrStation] != -1) {
                connFront.add(arrTime + walkTimes[arrStation], 0, connIdx);
            }

            // Option 2: Continuer dans le même véhicule
            if (profile.forTrip(tripId) != null) {
                connFront.addAll(profile.forTrip(tripId));
            }

            // Option 3: Changer de véhicule
            if (profile.forStation(arrStation) != null) {
                int finalConnIdx = connIdx;
                profile.forStation(arrStation).forEach(t -> {
                    if (depMins(t) >= arrTime) {
                        long tuple = PackedCriteria.withoutDepMins(PackedCriteria.withAdditionalChange(PackedCriteria.withPayload(t, finalConnIdx)));
                        connFront.add(tuple);
                    }
                });
            }

            //Optimisation 1
            if (connFront.isEmpty()) {
                continue;
            }

            // Mise à jour de la frontière de la course
            if (profile.forTrip(tripId) == null) {
                profile.setForTrip(tripId, connFront);
            } else {
                profile.forTrip(tripId).addAll(connFront);
            }


            // Mise à jour des gares accessibles
            int depStation = timeTable.stationId(connections.depStopId(connIdx));
            int depTime = connections.depMins(connIdx);
            int accessInterval = transfers.arrivingAt(depStation);

            for (int j = PackedRange.startInclusive(accessInterval); j < PackedRange.endExclusive(accessInterval); j++) {
                int walkFromStation = transfers.depStationId(j);
                int adjustedTime = depTime - transfers.minutes(j);

                ParetoFront.Builder stationFront = new ParetoFront.Builder();
                int finalConnIdx1 = connIdx;
                connFront.forEach(t -> {
                    int stops = connections.tripPos(payload(t)) - connections.tripPos(finalConnIdx1);
                    long updated = withDepMins(withPayload(t, Bits32_24_8.pack(finalConnIdx1, stops)), adjustedTime);
                    stationFront.add(updated);
                });


                if (profile.forStation(walkFromStation) != null) {
                    //optimisation 2
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