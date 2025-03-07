package ch.epfl.rechor.timetable;

public interface Stations extends Indexed {
    abstract String name(int id) throws IndexOutOfBoundsException;

    abstract double longitude(int id) throws IndexOutOfBoundsException;

    abstract double latitude(int id) throws IndexOutOfBoundsException;
}
