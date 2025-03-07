package ch.epfl.rechor.timetable;

public interface Transfers extends Indexed {
    abstract int depStationId(int id) throws IndexOutOfBoundsException;

    abstract int minutes(int id) throws IndexOutOfBoundsException;

    abstract int arrivingAt(int stationId) throws IndexOutOfBoundsException;

    abstract int minutesBetween(int depStationId, int arrStationId) throws IndexOutOfBoundsException;
}
