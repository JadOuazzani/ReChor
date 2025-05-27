package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for converting a {@link Journey} into a GeoJSON document.
 * <p>
 * Produces a GeoJSON {@code LineString} whose coordinates correspond to all
 * stops of the journey (departure, intermediate, and arrival). Coordinates are
 * rounded to five decimal places and consecutive identical points are omitted
 * to minimize the length of the output. The resulting JSON contains no
 * unnecessary whitespace.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class JourneyGeoJsonConverter {

    /**
     * Private constructor to prevent instantiation.
     */
    private JourneyGeoJsonConverter() {
    }

    /**
     * Converts the given journey into a GeoJSON document representing its itinerary.
     * <p>
     * The returned {@link Json} object is a {@code JObject} with two entries:
     * <ul>
     *   <li>{@code "type"}: the literal {@code "LineString"}</li>
     *   <li>{@code "coordinates"}: a {@code JArray} of {@code JArray} pairs,
     *       each pair representing [longitude, latitude] of a stop.</li>
     * </ul>
     * Coordinates are rounded to five decimal places, and any pair identical
     * to the immediately preceding one is omitted to keep the GeoJSON compact.
     * </p>
     *
     * @param journey the {@link Journey} to convert; must not be {@code null}
     * @return a {@link Json} object representing the GeoJSON LineString of the journey
     * @throws NullPointerException if {@code journey} is {@code null}
     */
    public static Json toGeoJson(Journey journey) {
        List<Journey.Leg> legs = Objects.requireNonNull(journey).legs();
        List<Json> coordinates = new ArrayList<>();
        Json.JNumber lastLon = null;
        Json.JNumber lastLat = null;

        for (Journey.Leg leg : legs) {
            // Add the departure stop of the leg
            Stop departureStop = leg.depStop();
            addUniqueCoordinate(coordinates, departureStop, lastLon, lastLat);
            lastLon = new Json.JNumber(roundCoordinate(departureStop.longitude()));
            lastLat = new Json.JNumber(roundCoordinate(departureStop.latitude()));

            // Add intermediate stops of the leg
            List<Journey.Leg.IntermediateStop> intermediateStops = leg.intermediateStops();
            for (Journey.Leg.IntermediateStop intermediateStop : intermediateStops) {
                Stop stop = intermediateStop.stop();
                addUniqueCoordinate(coordinates, stop, lastLon, lastLat);
                lastLon = new Json.JNumber(roundCoordinate(stop.longitude()));
                lastLat = new Json.JNumber(roundCoordinate(stop.latitude()));
            }

            // Add the arrival stop of the leg
            Stop arrivalStop = leg.arrStop();
            addUniqueCoordinate(coordinates, arrivalStop, lastLon, lastLat);
            lastLon = new Json.JNumber(roundCoordinate(arrivalStop.longitude()));
            lastLat = new Json.JNumber(roundCoordinate(arrivalStop.latitude()));
        }

        return new Json.JObject(Map.of(
                new Json.JString("type"), new Json.JString("LineString"),
                new Json.JString("coordinates"), new Json.JArray(coordinates)
        ));
    }

    /**
     * Adds the stop's [longitude, latitude] coordinate array to the list if it
     * differs from the last added coordinate pair.
     *
     * @param coordinates   the list of JSON coordinate arrays to append to
     * @param stop          the {@link Stop} whose coordinates to add
     * @param lastLongitude the last added longitude value, or {@code null} if none
     * @param lastLatitude  the last added latitude value, or {@code null} if none
     */
    private static void addUniqueCoordinate(List<Json> coordinates,
                                            Stop stop,
                                            Json.JNumber lastLongitude,
                                            Json.JNumber lastLatitude) {
        Json.JNumber currentLongitude = new Json.JNumber(roundCoordinate(stop.longitude()));
        Json.JNumber currentLatitude  = new Json.JNumber(roundCoordinate(stop.latitude()));

        if ((lastLongitude == null && lastLatitude == null)
                || !(currentLongitude.equals(lastLongitude)
                && currentLatitude.equals(lastLatitude))) {
            coordinates.add(new Json.JArray(List.of(currentLongitude, currentLatitude)));
        }
    }

    /**
     * Rounds a geographic coordinate (longitude or latitude) to five decimal places.
     *
     * @param coordinate the original coordinate value
     * @return the coordinate rounded to five decimal places
     */
    private static double roundCoordinate(double coordinate) {
        return Math.round(coordinate * 1e5) / 1e5;
    }

}
