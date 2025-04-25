package ch.epfl.rechor.timetable;

/**
 * Connections represents indexed connections.
 * <p>
 * For the needs of the journey search algorithm, connections must be ordered by departure time in descending order.
 * </p>
 *
 * <p>
 * The indices for departure and arrival stops returned by {@code depStopId} and {@code arrStopId} may refer either
 to stations or to platforms.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public interface Connections extends Indexed {

    /**
     * Returns the index of the departure stop for the connection at the specified index.
     *
     * @param id the index of the connection
     * @return the index of the departure stop
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    int depStopId(int id);

    /**
     * Returns the departure time for the connection at the specified index,
     * expressed in minutes after midnight.
     *
     * @param id the index of the connection
     * @return the departure time in minutes after midnight
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    int depMins(int id);

    /**
     * Returns the index of the arrival stop for the connection at the specified index.
     *
     * @param id the index of the connection
     * @return the index of the arrival stop
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    int arrStopId(int id);

    /**
     * Returns the arrival time for the connection at the specified index,
     * expressed in minutes after midnight.
     *
     * @param id the index of the connection
     * @return the arrival time in minutes after midnight
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    int arrMins(int id);

    /**
     * Returns the index of the trip to which the connection at the specified index belongs.
     *
     * @param id the index of the connection
     * @return the trip index
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    int tripId(int id);

    /**
     * Returns the position of the connection within its trip.
     * The first connection in a trip has index 0.
     *
     * @param id the index of the connection
     * @return the position of the connection in its trip
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    int tripPos(int id);

    /**
     * Returns the index of the connection following the connection at the specified index in its trip,
     * or the index of the first connection of the trip if the connection at the given index is the last one.
     *
     * @param id the index of the current connection
     * @return the index of the next connection in the trip
     * @throws IndexOutOfBoundsException if {@code id} is less than 0 or greater than or equal to {@code size()}
     */
    int nextConnectionId(int id);
}
