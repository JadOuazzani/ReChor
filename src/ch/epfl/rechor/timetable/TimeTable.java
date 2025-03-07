package ch.epfl.rechor.timetable;

import java.time.LocalDate;

public interface TimeTable {
    Stations stations();

    StationAliases stationAliases();

    Platforms platforms();

    Routes routes();

    Transfers transfers();

    Trips tripsFor(LocalDate date);

    Connections connectionsFor(LocalDate date);

    default boolean isStationId(int stopId) {
        return stopId < stations().size();
    }

    default boolean isPlatformId(int stopId) {
        return !isStationId(stopId) && stopId < stations().size() + platforms().size();
    }

    default int stationId(int stopId) {
        if (isStationId(stopId)) {
            return stopId;
        } else {
            return platforms().stationId(stopId - stations().size());
        }
    }

    default String platformName(int stopId) {
        if (isPlatformId(stopId)) {
            return platforms().name(stopId - stations().size());
        }
        return null;
    }
}
