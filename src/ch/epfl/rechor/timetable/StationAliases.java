package ch.epfl.rechor.timetable;

public interface StationAliases extends Indexed{
    abstract String alias(int id) throws IndexOutOfBoundsException;
    abstract String stationName(int id) throws IndexOutOfBoundsException;
}
