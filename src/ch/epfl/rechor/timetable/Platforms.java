package ch.epfl.rechor.timetable;

public interface Platforms extends Indexed {
    abstract String name(int id) throws IndexOutOfBoundsException;
    abstract int stationId(int id) throws IndexOutOfBoundsException;
}
