    package ch.epfl.rechor.journey;

    import ch.epfl.rechor.PackedRange;
    import ch.epfl.rechor.Bits32_24_8;
    import ch.epfl.rechor.timetable.Connections;
    import ch.epfl.rechor.timetable.Stations;
    import ch.epfl.rechor.timetable.TimeTable;
    import ch.epfl.rechor.timetable.Transfers;
    import ch.epfl.rechor.timetable.mapped.FileTimeTable;

    import java.io.IOException;
    import java.nio.file.Path;
    import java.time.LocalDate;
    import java.time.Month;
    import java.util.Arrays;

    public record Router(TimeTable tt) {
        public Profile profile(LocalDate date, int destId) {
            Profile.Builder p = new Profile.Builder(tt, date, destId);
            Connections connections = tt.connectionsFor(date);
            Transfers transfers = tt.transfers();
            int[] walkingTimeTable = walkingTable(destId);

            for (int i = 0; i < connections.size(); i++) {
                ParetoFront.Builder f = new ParetoFront.Builder();
                int arrivalStopId = tt.stationId(connections.arrStopId(i));
                int arrivalTime = connections.arrMins(i);
                int tripId = connections.tripId(i);
                int depStopId = tt.stationId(connections.depStopId(i));
                int depTime = connections.depMins(i);

                // Option 1: Marcher de l'arrivée à la destination
                if (walkingTimeTable[arrivalStopId] != -1) {
                    int walkingTime = walkingTimeTable[arrivalStopId];
                    int payload = Bits32_24_8.pack(i, 0);
                    f.add(arrivalTime + walkingTime, 0, payload);
                }

                // Option 2: Continuer avec la liaison suivante
                ParetoFront.Builder profileTrip = p.forTrip(tripId);
                if (profileTrip != null) {
                    f.addAll(profileTrip);
                }

                // Option 3: Changer de véhicule à l'arrivée
                ParetoFront.Builder arrivalStationFront = p.forStation(arrivalStopId);
                if (arrivalStationFront != null) {
                    int finalI = i;
                    arrivalStationFront.forEach(tuple -> {
                        if (PackedCriteria.depMins(tuple) >= arrivalTime) {
                            int updatedPayload = Bits32_24_8.pack(finalI, 0);
                            long updatedTuple = PackedCriteria.withoutDepMins(
                                    PackedCriteria.withPayload(
                                            PackedCriteria.withAdditionalChange(tuple),
                                            updatedPayload)
                            );
                            f.add(updatedTuple);
                        }
                    });
                }

                // Mise à jour du trip
                if (profileTrip == null)
                    p.setForTrip(tripId, f);
                else
                    profileTrip.addAll(f);


                int interval = transfers.arrivingAt(depStopId);
                for (int j = PackedRange.startInclusive(interval); j < PackedRange.endExclusive(interval); j++) {
                    int d = depTime - transfers.minutes(j);
                    ParetoFront.Builder paretoBuildWithDep = new ParetoFront.Builder();

                    int finalI = i;
                    f.forEach((t) -> {
                        int intermediatesStop = connections.tripPos(Bits32_24_8.unpack24(PackedCriteria.payload(t))) -
                                connections.tripPos(finalI);
                        int payload = Bits32_24_8.pack(finalI, intermediatesStop);
                        long updatesTuple = PackedCriteria.withDepMins(
                                PackedCriteria.withPayload(t,
                                        payload
                                ),
                                d
                        );
                        paretoBuildWithDep.add(updatesTuple);
                    });

                    int walkFromStopId = transfers.depStationId(j);
                    if (p.forStation(walkFromStopId) == null)
                        p.setForStation(walkFromStopId, paretoBuildWithDep);
                    else
                        p.forStation(walkFromStopId).addAll(paretoBuildWithDep);
                }
            }
            return p.build();
        }

        private int[] walkingTable(int arrStationId) {
            int[] walkingTimes = new int[tt.stations().size()];
            Arrays.fill(walkingTimes, -1);
            Transfers transfers = tt.transfers();

            int arrivingAt = transfers.arrivingAt(arrStationId);
            int startInclusive = PackedRange.startInclusive(arrivingAt);
            int endExclusive = PackedRange.endExclusive(arrivingAt);
            for (int i = startInclusive; i < endExclusive; i++) {
                int depStationId = transfers.depStationId(i);
                int transferTime = transfers.minutes(i);
                walkingTimes[depStationId] = transferTime;
            }
            return walkingTimes;
        }


        static int stationId(Stations stations, String stationName) {
            for (int i = 0; i < stations.size(); i++) {
                if (stations.name(i).equals(stationName)) {
                    return i;
                }
            }
            throw new IllegalArgumentException("Station not found: " + stationName);
        }

        public static void main(String[] args) throws IOException {
            TimeTable timetable = FileTimeTable.in(Path.of("timetable"));
            Stations stations = timetable.stations();
            LocalDate date = LocalDate.of(2025, Month.APRIL, 1);
            int depStationId = stationId(stations, "Ecublens VD, EPFL");
            int arrStationId = stationId(stations, "Gruyères");

            Router router = new Router(timetable);
            Profile profile = router.profile(date, arrStationId);

            Journey j = JourneyExtractor.journeys(profile, depStationId).get(32);
            System.out.println(JourneyIcalConverter.toIcalendar(j));
        }
    }