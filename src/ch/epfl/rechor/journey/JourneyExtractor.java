package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static ch.epfl.rechor.journey.PackedCriteria.*;

public final class JourneyExtractor {
    private JourneyExtractor() {}

    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        TimeTable timeTable = profile.timeTable();

        Connections connections = timeTable.connectionsFor(profile.date());
        Trips trip = timeTable.tripsFor(profile.date());
        Routes routes = timeTable.routes();
        Stations stations = timeTable.stations();
        Transfers transfers = timeTable.transfers();
        Platforms platforms = timeTable.platforms();

        LocalDateTime baseDate = LocalDateTime.of(profile.date(), LocalDateTime.MIN.toLocalTime());

        profile.forStation(depStationId).forEach(criteria -> {
            List<Journey.Leg> legs = new ArrayList<>();

            // Décodage des critères
            int depTimeSec = depMins(criteria) * 60;
            int arrTimeSec = arrMins(criteria) * 60;
            int totalChanges = changes(criteria);
            int remainingChanges = totalChanges;
            int payload = payload(criteria);

            // Extraction des infos de la première étape
            int firstConnectionId = (payload >> 8) & 0xFFFFFF;
            int stopCount = payload & 0xFF;
            int currentConnectionId = firstConnectionId;

            // Gestion de l'étape à pied initiale si nécessaire
            int firstDepStopId = connections.depStopId(firstConnectionId);
            int firstDepStationId = timeTable.stationId(firstDepStopId);

            if (firstDepStationId != depStationId) {
                legs.add(new Journey.Leg.Foot(
                        new Stop(
                                stations.name(depStationId),
                                "",
                                stations.longitude(depStationId),
                                stations.latitude(depStationId)
                        ),
                        baseDate.plusSeconds(depTimeSec),
                        new Stop(
                                stations.name(firstDepStationId),
                                firstDepStopId >= stations.size() ?
                                        platforms.name(firstDepStopId - stations.size()) : "",
                                stations.longitude(firstDepStationId),
                                stations.latitude(firstDepStationId)
                        ),
                        baseDate.plusSeconds(depTimeSec).plusMinutes(
                                transfers.minutesBetween(depStationId, firstDepStationId)
                        )
                ));
            }

            // Construire l'étape de transport initiale
            List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
            int tripId = connections.tripId(firstConnectionId);
            String routeName = routes.name(trip.routeId(tripId));
            String destination = trip.destination(tripId);
            Vehicle vehicle = routes.vehicle(trip.routeId(tripId));

            // Traiter les arrêts intermédiaires
            for (int i = 0; i < stopCount; i++) {
                currentConnectionId = connections.nextConnectionId(currentConnectionId);
                intermediateStops.add(new Journey.Leg.IntermediateStop(
                        new Stop(
                                stations.name(timeTable.stationId(connections.arrStopId(currentConnectionId))),
                                connections.arrStopId(currentConnectionId) >= stations.size() ?
                                        platforms.name(connections.arrStopId(currentConnectionId) - stations.size()) : "",
                                stations.longitude(timeTable.stationId(connections.arrStopId(currentConnectionId))),
                                stations.latitude(timeTable.stationId(connections.arrStopId(currentConnectionId)))
                        ),
                        baseDate.plusSeconds(connections.arrMins(currentConnectionId) * 60L),
                        baseDate.plusSeconds(connections.depMins(currentConnectionId) * 60L)
                ));
            }

            // Ajouter l'étape de transport principale
            legs.add(new Journey.Leg.Transport(
                    new Stop(
                            stations.name(firstDepStationId),
                            firstDepStopId >= stations.size() ?
                                    platforms.name(firstDepStopId - stations.size()) : "",
                            stations.longitude(firstDepStationId),
                            stations.latitude(firstDepStationId)
                    ),
                    baseDate.plusSeconds(connections.depMins(firstConnectionId) * 60L),
                    new Stop(
                            stations.name(timeTable.stationId(connections.arrStopId(currentConnectionId))),
                            connections.arrStopId(currentConnectionId) >= stations.size() ?
                                    platforms.name(connections.arrStopId(currentConnectionId) - stations.size()) : "",
                            stations.longitude(timeTable.stationId(connections.arrStopId(currentConnectionId))),
                            stations.latitude(timeTable.stationId(connections.arrStopId(currentConnectionId)))
                    ),
                    baseDate.plusSeconds(connections.arrMins(currentConnectionId) * 60L),
                    intermediateStops,
                    vehicle,
                    routeName,
                    destination
            ));

            // Variables de suivi
            int currentArrStationId = timeTable.stationId(connections.arrStopId(currentConnectionId));
            LocalDateTime currentArrTime = baseDate.plusSeconds(connections.arrMins(currentConnectionId) * 60L);

            // Traitement des correspondances
            while (remainingChanges > 0) {
                // Calcul du nombre de changements déjà effectués
                final int changesUsed = totalChanges - remainingChanges;

                // Recherche du prochain critère
                long nextCriteria = -1;

                // Solution pour itérer sur les critères de la station
                List<Long> stationCriteria = new ArrayList<>();
                profile.forStation(currentArrStationId).forEach(stationCriteria::add);

                for (long c : stationCriteria) {
                    if (arrMins(c) == arrTimeSec/60 && changes(c) == changesUsed) {
                        nextCriteria = c;
                        break;
                    }
                }

                if (nextCriteria == -1) break;

                int nextPayload = payload(nextCriteria);
                int nextConnectionId = nextPayload >> 8;
                int nextStopCount = nextPayload & 0xFF;

                // Étape à pied (correspondance)
                int nextDepStopId = connections.depStopId(nextConnectionId);
                int nextDepStationId = timeTable.stationId(nextDepStopId);

                legs.add(new Journey.Leg.Foot(
                        new Stop(
                                stations.name(currentArrStationId),
                                connections.arrStopId(nextConnectionId) >= stations.size() ?
                                        platforms.name(connections.arrStopId(nextConnectionId) - stations.size()) : "",
                                stations.longitude(currentArrStationId),
                                stations.latitude(currentArrStationId)
                        ),
                        currentArrTime,
                        new Stop(
                                stations.name(nextDepStationId),
                                nextDepStopId >= stations.size() ?
                                        platforms.name(nextDepStopId - stations.size()) : "",
                                stations.longitude(nextDepStationId),
                                stations.latitude(nextDepStationId)
                        ),
                        currentArrTime.plusMinutes(
                                transfers.minutesBetween(currentArrStationId, nextDepStationId)
                        )
                ));

                // Préparer la prochaine étape de transport
                intermediateStops = new ArrayList<>();
                tripId = connections.tripId(nextConnectionId);
                routeName = routes.name(trip.routeId(tripId));
                destination = trip.destination(tripId);
                vehicle = routes.vehicle(trip.routeId(tripId));

                // Traiter les arrêts intermédiaires
                int nextCurrentConnectionId = nextConnectionId;
                for (int i = 0; i < nextStopCount; i++) {
                    nextCurrentConnectionId = connections.nextConnectionId(nextCurrentConnectionId);
                    intermediateStops.add(new Journey.Leg.IntermediateStop(
                            new Stop(
                                    stations.name(timeTable.stationId(connections.arrStopId(nextCurrentConnectionId))),
                                    connections.arrStopId(nextCurrentConnectionId) >= stations.size() ?
                                            platforms.name(connections.arrStopId(nextCurrentConnectionId) - stations.size()) : "",
                                    stations.longitude(timeTable.stationId(connections.arrStopId(nextCurrentConnectionId))),
                                    stations.latitude(timeTable.stationId(connections.arrStopId(nextCurrentConnectionId)))
                            ),
                            baseDate.plusSeconds(connections.arrMins(nextCurrentConnectionId) * 60L),
                            baseDate.plusSeconds(connections.depMins(nextCurrentConnectionId) * 60L)
                    ));
                }

                // Ajouter l'étape de transport
                legs.add(new Journey.Leg.Transport(
                        new Stop(
                                stations.name(nextDepStationId),
                                nextDepStopId >= stations.size() ?
                                        platforms.name(nextDepStopId - stations.size()) : "",
                                stations.longitude(nextDepStationId),
                                stations.latitude(nextDepStationId)
                        ),
                        currentArrTime.plusMinutes(
                                transfers.minutesBetween(currentArrStationId, nextDepStationId)
                        ),
                        new Stop(
                                stations.name(timeTable.stationId(connections.arrStopId(nextCurrentConnectionId))),
                                connections.arrStopId(nextCurrentConnectionId) >= stations.size() ?
                                        platforms.name(connections.arrStopId(nextCurrentConnectionId) - stations.size()) : "",
                                stations.longitude(timeTable.stationId(connections.arrStopId(nextCurrentConnectionId))),
                                stations.latitude(timeTable.stationId(connections.arrStopId(nextCurrentConnectionId)))
                        ),
                        baseDate.plusSeconds(connections.arrMins(nextCurrentConnectionId) * 60L),
                        intermediateStops,
                        vehicle,
                        routeName,
                        destination
                ));

                // Mise à jour des variables
                currentArrStationId = timeTable.stationId(connections.arrStopId(nextCurrentConnectionId));
                currentArrTime = baseDate.plusSeconds(connections.arrMins(nextCurrentConnectionId) * 60L);
                remainingChanges--;
            }

            // Étape à pied finale si nécessaire
            if (currentArrStationId != profile.arrStationId()) {
                legs.add(new Journey.Leg.Foot(
                        new Stop(
                                stations.name(currentArrStationId),
                                "",
                                stations.longitude(currentArrStationId),
                                stations.latitude(currentArrStationId)
                        ),
                        currentArrTime,
                        new Stop(
                                stations.name(profile.arrStationId()),
                                "",
                                stations.longitude(profile.arrStationId()),
                                stations.latitude(profile.arrStationId())
                        ),
                        currentArrTime.plusMinutes(
                                transfers.minutesBetween(currentArrStationId, profile.arrStationId())
                        )
                ));
            }

            journeys.add(new Journey(legs));
        });

        // Tri final
        journeys.sort(Comparator
                .comparing(Journey::depTime)
                .thenComparing(Journey::arrTime));

        return journeys;
    }
}