package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * A {@link TimeTable} implementation that caches the date‐dependent data (connections
 * and trips) for a single day to avoid repeated loading from the underlying source.
 * <p>
 * On the first call to {@link #tripsFor(LocalDate)} or {@link #connectionsFor(LocalDate)},
 * the data for that date is fetched from the wrapped {@link TimeTable} and stored.
 * Subsequent calls for the same date return the cached data. If a different date is
 * requested, the cache is refreshed by reloading from the underlying timetable.
 *
 * @implements TimeTable
 *
 *@author Riad Elhariry
 *@author Jad Chahed Ouazzani
 */
public class CachedTimeTable implements TimeTable {
    private TimeTable underlyingTime;
    private LocalDate localDate;
    private Connections connections;
    private Trips trips;

    /**
     * Creates a new {@code CachedTimeTable} that delegates to the given
     * {@code TimeTable} for actual data loading.
     *
     * @param time the underlying {@link TimeTable} whose data will be cached
     */
    public CachedTimeTable(TimeTable time) {
        this.underlyingTime = time;
    }

    /**
     * Returns the complete set of stations.
     * <p>This always delegates directly to the underlying timetable.</p>
     *
     * @return the {@link Stations} provided by the underlying timetable
     */
    @Override
    public Stations stations() {
        return underlyingTime.stations();
    }

    /**
     * Returns all station aliases.
     * <p>This always delegates directly to the underlying timetable.</p>
     *
     * @return the {@link StationAliases} provided by the underlying timetable
     */
    @Override
    public StationAliases stationAliases() {
        return underlyingTime.stationAliases();
    }

    /**
     * Returns the set of platforms.
     * <p>This always delegates directly to the underlying timetable.</p>
     *
     * @return the {@link Platforms} provided by the underlying timetable
     */
    @Override
    public Platforms platforms() {
        return underlyingTime.platforms();
    }

    /**
     * Returns the collection of routes.
     * <p>This always delegates directly to the underlying timetable.</p>
     *
     * @return the {@link Routes} provided by the underlying timetable
     */
    @Override
    public Routes routes() {
        return underlyingTime.routes();
    }

    /**
     * Returns all transfers (walking times between stations).
     * <p>This always delegates directly to the underlying timetable.</p>
     *
     * @return the {@link Transfers} provided by the underlying timetable
     */
    @Override
    public Transfers transfers() {
        return underlyingTime.transfers();
    }

    /**
     * Returns the {@link Trips} scheduled for the given date, using the cache
     * to avoid reloading if the date has not changed.
     *
     * @param date the {@link LocalDate} for which trips are requested
     * @return the {@link Trips} for the specified date
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        updateCash(date);
        return trips;
    }

    /**
     * Returns the {@link Connections} scheduled for the given date, using the cache
     * to avoid reloading if the date has not changed.
     *
     * @param date the {@link LocalDate} for which connections are requested
     * @return the {@link Connections} for the specified date
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        updateCash(date);
        return connections;
    }

    /**
     * Checks whether the requested date differs from the currently cached date.
     * If so, reloads both trips and connections for the new date from the
     * underlying timetable and updates the cache.
     *
     * @param date the {@link LocalDate} to ensure is cached
     */
    private void updateCash(LocalDate date) {
        if (!date.equals(localDate)) {
            this.localDate = date;
            this.trips = underlyingTime.tripsFor(localDate);
            this.connections = underlyingTime.connectionsFor(localDate);
        }
    }
}
