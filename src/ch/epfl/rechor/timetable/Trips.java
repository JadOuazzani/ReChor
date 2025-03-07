package ch.epfl.rechor.timetable;

public interface Trips extends Indexed {
    abstract int routeId(int id) throws IndexOutOfBoundsException;

    abstract String destination(int id) throws IndexOutOfBoundsException;
}
