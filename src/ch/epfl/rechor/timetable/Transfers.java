package ch.epfl.rechor.timetable;

import java.util.NoSuchElementException;

/**
 * Transfers represents indexed transfers.
 * <p>
 * Note: In the data we use, transfers are only possible between (or within) stations,
 * and not between platforms.
 * </p>
 *
 * <p>This interface provides the following abstract methods:
 * <ul>
 *   <li>{@code int depStationId(int id)} – returns the index of the departure station for the transfer at the given index;</li>
 *   <li>{@code int minutes(int id)} – returns the duration, in minutes, of the transfer at the given index;</li>
 *   <li>{@code int arrivingAt(int stationId)} – returns the packed interval (according to the {@code PackedRange} convention)
 *       of the indices of transfers whose arrival station is the station with the given index;</li>
 *   <li>{@code int minutesBetween(int depStationId, int arrStationId)} – returns the duration, in minutes, of the transfer
 *       between the two stations with the given indices, or throws a {@link NoSuchElementException} if no transfer is possible
 *       between these stations.</li>
 * </ul>
 * </p>
 *
 * <p>All these methods throw an {@link IndexOutOfBoundsException} if any provided index is invalid,
 * that is, if it is less than 0 or greater than or equal to the value returned by {@link Indexed#size()}.
 * </p>
 */
public interface Transfers extends Indexed {
    /**
     * Returns the index of the departure station for the transfer at the specified index.
     *
     * @param id the index of the transfer
     * @return the index of the departure station
     * @throws IndexOutOfBoundsException if {@code id} is invalid
     */
    int depStationId(int id) ;

    /**
     * Returns the duration, in minutes, of the transfer at the specified index.
     *
     * @param id the index of the transfer
     * @return the transfer duration in minutes
     * @throws IndexOutOfBoundsException if {@code id} is invalid
     */
    int minutes(int id) ;

    /**
     * Returns the packed interval (according to the {@code PackedRange} convention) of the indices of transfers
     * whose arrival station is the station with the specified index.
     *
     * @param stationId the index of the arrival station
     * @return the packed interval of transfer indices arriving at the specified station
     * @throws IndexOutOfBoundsException if {@code stationId} is invalid
     */
    int arrivingAt(int stationId) ;

    /**
     * Returns the duration, in minutes, of the transfer between the two stations with the specified indices.
     *
     * @param depStationId the index of the departure station
     * @param arrStationId the index of the arrival station
     * @return the duration of the transfer in minutes
     * @throws IndexOutOfBoundsException if either {@code depStationId} or {@code arrStationId} is invalid
     * @throws NoSuchElementException if no transfer is possible between the specified stations
     */
    int minutesBetween(int depStationId, int arrStationId) ;
}
