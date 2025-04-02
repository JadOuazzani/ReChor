package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;
import ch.epfl.rechor.timetable.mapped.BufferedStations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record Profile(TimeTable timeTable, LocalDate date, int arrStationId, List<ParetoFront> stationFront) {

    public Profile {
        stationFront = List.copyOf(stationFront);
    }

    public Connections connections() {
        return timeTable.connectionsFor(date);
    }

    public Trips trips(){
        return timeTable.tripsFor(date);
    }

    public ParetoFront forStation(int stationId) {
        if (stationId < 0 || stationId >= stationFront.size()) {
            throw new IndexOutOfBoundsException();
        }
        return stationFront.get(stationId);
    }

    public static final class Builder {
        private final TimeTable timeTable;
        private final LocalDate date;
        private final int arrStationId;
        private final ParetoFront.Builder[] stationBuilders;
        private final ParetoFront.Builder[] tripBuilders;

        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            this.stationBuilders = new ParetoFront.Builder[timeTable.stations().size()];
            this.tripBuilders = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
        }

        public ParetoFront.Builder forStation(int stationId) {
            if (stationId < 0 || stationId >= stationBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            return stationBuilders[stationId];
        }

        public void setForStation(int stationId, ParetoFront.Builder builder){
            if (stationId < 0 || stationId >= stationBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            stationBuilders[stationId] = builder;
        }

        public ParetoFront.Builder forTrip(int tripId){
            if (tripId < 0 || tripId >= tripBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            return tripBuilders[tripId];
        }

        public void setForTrip(int tripId, ParetoFront.Builder builder){
            if (tripId < 0 || tripId >= tripBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            tripBuilders[tripId] = builder;
        }

        public Profile build(){
            List<ParetoFront> stationFront = new ArrayList<>(stationBuilders.length);
            for (ParetoFront.Builder builder : stationBuilders) {
                if (builder == null) {
                    stationFront.add(ParetoFront.EMPTY);
                } else {
                    stationFront.add(builder.build());
                }
            }
            return new Profile(timeTable, date, arrStationId, stationFront);
        }
    }
}