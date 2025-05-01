package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JourneyGeoJsonConverter {
    private JourneyGeoJsonConverter(){}

    /**
     * Converts a journey into a GeoJSON document representing its itinerary.
     * The GeoJSON document contains a single LineString whose coordinates are
     * all the stops of the journey (departure, arrival, and intermediate).
     * Longitudes and latitudes are rounded to 5 decimal places, and consecutive
     * identical coordinates are omitted to produce the shortest possible document.
     * The resulting JSON document contains no unnecessary whitespace.
     *
     * @param journey The journey to convert.
     * @return A Json object representing the GeoJSON document.
     */
    public static Json toGeoJson(Journey journey) {
        List<Journey.Leg> legs = journey.legs();
        List<Json> coordinates = new ArrayList<>();
        Json.JNumber lastLon = null;
        Json.JNumber lastLat = null;

        // Add the departure stop of the journey
        Stop firstStop = journey.depStop();
        addUniqueCoordinate(coordinates, firstStop, lastLon, lastLat);
        lastLon = new Json.JNumber(roundCoordinate(firstStop.longitude()));
        lastLat = new Json.JNumber(roundCoordinate(firstStop.latitude()));

        for (Journey.Leg leg : legs) {
            // Add the departure stop of the leg (already handled for the first leg)
            Stop departureStop = leg.depStop();
            if (!departureStop.equals(firstStop) || legs.indexOf(leg) > 0) {
                addUniqueCoordinate(coordinates, departureStop, lastLon, lastLat);
                lastLon = new Json.JNumber(roundCoordinate(departureStop.longitude()));
                lastLat = new Json.JNumber(roundCoordinate(departureStop.latitude()));
            }

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

        Json.JObject geoJson = new Json.JObject(Map.of(
                "type", new Json.JString("LineString"),
                "coordinates", new Json.JArray(coordinates)
        ));
        return geoJson;
    }

    private static double roundCoordinate(double coordinate) {
        return Math.round(coordinate * 1e5) / 1e5;
    }

    private static void addUniqueCoordinate(List<Json> coordinates, Stop stop, Json.JNumber lastLongitude, Json.JNumber lastLatitude) {
        double longitude = roundCoordinate(stop.longitude());
        double latitude = roundCoordinate(stop.latitude());
        Json.JNumber currentLongitude = new Json.JNumber(longitude);
        Json.JNumber currentLatitude = new Json.JNumber(latitude);

        if (lastLongitude == null || !currentLongitude.equals(lastLongitude) || !currentLatitude.equals(lastLatitude)) {
            coordinates.add(new Json.JArray(List.of(currentLongitude, currentLatitude)));
        }
    }
}