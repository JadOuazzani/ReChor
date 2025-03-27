package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static ch.epfl.rechor.timetable.mapped.FileTimeTable.in;

public record Profile(TimeTable timeTable, LocalDate date, int arrStationId, List<ParetoFront> stationFront) {

    public Profile {
        stationFront = List.copyOf(stationFront);
    }

    public Connections connections() throws IOException {
        return timeTable().connectionsFor(date());
    }

    public Trips trips() throws IOException {
        return timeTable().tripsFor(date());
    }

    public ParetoFront forStation(int stationId) {
        return stationFront().get(stationId);
    }

    public final static class Builder {
        final TimeTable timeTable;
        final LocalDate date;
        final int arrStationId;
        private ParetoFront.Builder[] stationsBuilder;
        private ParetoFront.Builder[] tripsBuilder;


        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            this.stationsBuilder = new ParetoFront.Builder[timeTable.stations().size()];
            this.tripsBuilder = new ParetoFront.Builder[timeTable.tripsFor(date).size()];

        }

        public ParetoFront.Builder forStation(int stationId) {
            return stationsBuilder[stationId];
        }

        public void setForStation(int stationId, ParetoFront.Builder builder) {
            stationsBuilder[stationId] = builder;
        }

        public ParetoFront.Builder forTrip(int tripId) {
            return tripsBuilder[tripId];
        }

        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            tripsBuilder[tripId] = builder;
        }

        public Profile build() {
            List<ParetoFront> stationFront = new ArrayList<>(stationsBuilder.length);
            for (int i = 0; i < stationsBuilder.length; i++) {
                if (stationsBuilder[i] != null) {
                    stationFront.add(stationsBuilder[i].build());
                } else {
                    stationFront.add(ParetoFront.EMPTY);
                }
            }
            return new Profile(timeTable, date, arrStationId, stationFront);
        }


    }
}
