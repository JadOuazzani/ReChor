package ch.epfl.rechor.timetable;

public interface Connections extends Indexed {
    abstract int depStopId(int id) throws IndexOutOfBoundsException;

    abstract int depMins(int id) throws IndexOutOfBoundsException;

    abstract int arrStopId(int id) throws IndexOutOfBoundsException;

    abstract int arrMins(int id) throws IndexOutOfBoundsException;

    abstract int tripId(int id) throws IndexOutOfBoundsException;

    abstract int tripPos(int id) throws IndexOutOfBoundsException;

    abstract int nextConnectionId(int id) throws IndexOutOfBoundsException;
}
