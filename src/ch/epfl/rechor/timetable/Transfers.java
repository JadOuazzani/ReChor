package ch.epfl.rechor.timetable;

public interface Transfers extends Indexed {
    abstract int depStationId(int id);

    abstract int minutes(int id);

    abstract int arrivingAt(int stationId);

    abstract int minutesBetween(int depStationId, int arrStationId);
}
