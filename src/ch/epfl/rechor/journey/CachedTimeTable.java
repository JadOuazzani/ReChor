package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CachedTimeTable implements TimeTable {
    private TimeTable time;
    private LocalDate cachedate;
    private Connections connections;
    private Trips trips;


    public CachedTimeTable(TimeTable time) {
        this.time = time;
        trips = null;
        connections = null;
        trips = null;
    }
    @Override
    public Stations stations() {
        return time.stations();
    }

    @Override
    public StationAliases stationAliases() {
        return time.stationAliases();
    }

    @Override
    public Platforms platforms() {
        return time.platforms();
    }

    @Override
    public Routes routes() {
        return time.routes();
    }

    @Override
    public Transfers transfers() {
        return time.transfers();
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        if (date.equals(cachedate)){
            return trips;}
            this.cachedate = date;
            trips = time.tripsFor(date);
            return trips;
        }


    @Override
    public Connections connectionsFor(LocalDate date) {
        if (date.equals(cachedate)){
            return connections;
        }
        this.cachedate = date;
        connections = time.connectionsFor(date);
        return connections;
    }
}
