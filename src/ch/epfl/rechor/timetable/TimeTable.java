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
        return stopId >= stations().size();

    }

    default int stationId(int stopId) {
        return platforms().stationId(stopId);
    }

    default String platformName(int stopId) {
        if (isStationId(stopId)) {
            return null;
        } else {
            return platforms().name(stopId);
        }
    }
}
