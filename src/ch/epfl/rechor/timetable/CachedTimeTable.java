package ch.epfl.rechor.timetable;

import java.time.LocalDate;
import java.util.Objects;

public class CachedTimeTable implements TimeTable {
    private TimeTable underlyingTime;
    private LocalDate localDate;
    private Connections connections;
    private Trips trips;


    public CachedTimeTable(TimeTable time) {
        this.underlyingTime = time;
    }

    @Override
    public Stations stations() {
        return underlyingTime.stations();
    }

    @Override
    public StationAliases stationAliases() {
        return underlyingTime.stationAliases();
    }

    @Override
    public Platforms platforms() {
        return underlyingTime.platforms();
    }

    @Override
    public Routes routes() {
        return underlyingTime.routes();
    }

    @Override
    public Transfers transfers() {
        return underlyingTime.transfers();
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        updateCash(date);
        return trips;
    }

    @Override
    public Connections connectionsFor(LocalDate date) {
        updateCash(date);
        return connections;
    }

    private void updateCash(LocalDate date) {
        if (!date.equals(localDate)) {
            this.localDate = date;
            this.trips = underlyingTime.tripsFor(localDate);
            this.connections = underlyingTime.connectionsFor(localDate);
        }
    }
}
