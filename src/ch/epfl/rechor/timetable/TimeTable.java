package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Represents a public transport timetable.
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public interface TimeTable {

    /**
     * Returns the indexed stations of the timetable.
     *
     * @return the stations
     */
    Stations stations();

    /**
     * Returns the indexed alternative names for the stations.
     *
     * @return the station aliases
     */
    StationAliases stationAliases();

    /**
     * Returns the indexed platforms of the timetable.
     *
     * @return the platforms
     */
    Platforms platforms();

    /**
     * Returns the indexed routes (public transport lines) of the timetable.
     *
     * @return the routes
     */
    Routes routes();

    /**
     * Returns the indexed transfers of the timetable.
     *
     * @return the transfers
     */
    Transfers transfers();

    /**
     * Returns the indexed trips (runs) active on the given date.
     *
     * @param date the date for which trips are active
     * @return the trips active on the specified date
     */
    Trips tripsFor(LocalDate date);

    /**
     * Returns the indexed connections active on the given date.
     *
     * @param date the date for which connections are active
     * @return the connections active on the specified date
     */
    Connections connectionsFor(LocalDate date);

    /**
     * Returns {@code true} if and only if the given stop index corresponds to a station.
     *
     * @param stopId the stop index
     * @return {@code true} if the stop index is a station index; {@code false} otherwise
     */
    default boolean isStationId(int stopId) {
        return stopId < stations().size();
    }

    /**
     * Returns {@code true} if and only if the given stop index corresponds to a platform.
     *
     * @param stopId the stop index
     * @return {@code true} if the stop index is a platform index; {@code false} otherwise
     */
    default boolean isPlatformId(int stopId) {
        return !isStationId(stopId) && stopId < stations().size() + platforms().size();
    }

    /**
     * Returns the station index corresponding to the given stop index.
     * <p>
     * If the stop index represents a station, it is returned as is.
     * Otherwise, if the stop index represents a platform, the station index is computed using the platforms data.
     * </p>
     *
     * @param stopId the stop index
     * @return the station index corresponding to the given stop
     */
    default int stationId(int stopId) {
        if (isStationId(stopId)) {
            return stopId;
        } else {
            return platforms().stationId(stopId - stations().size());
        }
    }

    /**
     * Returns the name of the platform corresponding to the given stop index,
     * or {@code null} if the stop index represents a station.
     *
     * @param stopId the stop index
     * @return the platform name if the stop is a platform; {@code null} otherwise
     */
    default String platformName(int stopId) {
        if (isPlatformId(stopId)) {
            return platforms().name(stopId - stations().size());
        }
        return null;
    }
}
