package ch.epfl.rechor.journey;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ch.epfl.rechor.timetable.*;
import static ch.epfl.rechor.Bits32_24_8.*;
import static ch.epfl.rechor.journey.PackedCriteria.*;

public final class JourneyExtractor {
    private JourneyExtractor() {}

    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        profile.forStation(depStationId).forEach(criteria ->
                journeys.add(buildJourney(profile, depStationId, criteria)));
        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));
        return journeys;
    }

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

        // Handle initial foot leg
        if (firstDepStation != depStationId) {
            legs.add(new Journey.Leg.Foot(
                    createStop(tt, depStationId),
                    date.atStartOfDay().plusMinutes(depMins(criteria)).minusMinutes(1),
                    createStop(tt, firstDepStopId),
                    date.atStartOfDay().plusMinutes(depMins(criteria))
            ));
        }

        // Process all legs
        int currentConnId = firstConnId;
        int intermediateStops = unpack8(payload(criteria));
        int changes = changes(criteria);
        Stop currentArrStop = createStop(tt, firstArrStopId);

        for (int changesLeft = changes; changesLeft >= 0; changesLeft--) {
            if (changesLeft != changes) {
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

            // Process transport leg
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

        // Final foot leg if needed
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

    private static Stop createStop(TimeTable tt, int stopId) {
        int stationId = tt.stationId(stopId);
        return new Stop(
                tt.stations().name(stationId),
                tt.isPlatformId(stopId) ? tt.platformName(stopId) : "",
                tt.stations().longitude(stationId),
                tt.stations().latitude(stationId)
        );
    }
}