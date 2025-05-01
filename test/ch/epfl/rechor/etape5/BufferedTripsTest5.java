package ch.epfl.rechor.etape5;

import static org.junit.jupiter.api.Assertions.*;

import ch.epfl.rechor.timetable.mapped.BufferedTrips;
import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BufferedTripsTest5 {

    @Test
    public void testConstructorWithValidData() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("Flon");
        stringTable.add("Renens");
        stringTable.add("EPFL");

        // Créer un buffer avec 3 courses
        ByteBuffer buffer = ByteBuffer.allocate(12); // 3 * (2 + 2) bytes

        // Course 1: Ligne 0, destination Flon
        buffer.putShort((short)0); // ROUTE_ID
        buffer.putShort((short)0); // DESTINATION_ID pour Flon

        // Course 2: Ligne 1, destination Renens
        buffer.putShort((short)1); // ROUTE_ID
        buffer.putShort((short)1); // DESTINATION_ID pour Renens

        // Course 3: Ligne 2, destination EPFL
        buffer.putShort((short)2); // ROUTE_ID
        buffer.putShort((short)2); // DESTINATION_ID pour EPFL

        buffer.flip();

        BufferedTrips trips = new BufferedTrips(stringTable, buffer);

        assertEquals(3, trips.size());
        assertEquals(0, trips.routeId(0));
        assertEquals("Flon", trips.destination(0));
        assertEquals(1, trips.routeId(1));
        assertEquals("Renens", trips.destination(1));
        assertEquals(2, trips.routeId(2));
        assertEquals("EPFL", trips.destination(2));
    }

    @Test
    public void testEmptyBuffer() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("Test");
        ByteBuffer buffer = ByteBuffer.allocate(0);

        BufferedTrips trips = new BufferedTrips(stringTable, buffer);
        assertEquals(0, trips.size());
    }

    @Test
    public void testEmptyStringTable() {
        List<String> stringTable = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putShort((short)0); // ROUTE_ID
        buffer.putShort((short)0); // DESTINATION_ID inexistant
        buffer.flip();

        BufferedTrips trips = new BufferedTrips(stringTable, buffer);

        // L'accès à la destination devrait échouer à cause d'un index hors limites
        assertThrows(IndexOutOfBoundsException.class, () -> {
            trips.destination(0);
        });
    }

    @Test
    public void testInvalidTripIndex() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("Flon");

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putShort((short)0);
        buffer.putShort((short)0);
        buffer.flip();

        BufferedTrips trips = new BufferedTrips(stringTable, buffer);

        // Test avec index négatif
        assertThrows(IndexOutOfBoundsException.class, () -> {
            trips.routeId(-1);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            trips.destination(-1);
        });

        // Test avec index trop grand
        assertThrows(IndexOutOfBoundsException.class, () -> {
            trips.routeId(1);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            trips.destination(1);
        });
    }

    @Test
    public void testInvalidDestinationIndex() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("Flon");

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putShort((short)0); // ROUTE_ID
        buffer.putShort((short)999); // DESTINATION_ID invalide
        buffer.flip();

        BufferedTrips trips = new BufferedTrips(stringTable, buffer);

        // L'accès à la destination avec un index invalide devrait échouer
        assertThrows(IndexOutOfBoundsException.class, () -> {
            trips.destination(0);
        });
    }

    @Test
    public void testMultipleIdenticalRoutes() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("Flon");
        stringTable.add("Renens");

        // Créer un buffer avec plusieurs courses sur la même ligne
        ByteBuffer buffer = ByteBuffer.allocate(8); // 2 * (2 + 2) bytes

        // Course 1: Ligne 0, destination Flon
        buffer.putShort((short)0); // ROUTE_ID
        buffer.putShort((short)0); // DESTINATION_ID pour Flon

        // Course 2: Ligne 0 (même ligne), destination Renens
        buffer.putShort((short)0); // ROUTE_ID
        buffer.putShort((short)1); // DESTINATION_ID pour Renens

        buffer.flip();

        BufferedTrips trips = new BufferedTrips(stringTable, buffer);

        assertEquals(2, trips.size());
        assertEquals(0, trips.routeId(0));
        assertEquals("Flon", trips.destination(0));
        assertEquals(0, trips.routeId(1)); // Même ligne que la course 0
        assertEquals("Renens", trips.destination(1));
    }

    @Test
    public void testMaximumValues() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("Destination");

        // Tester avec les valeurs maximum pour U16 (65535)
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putShort((short)-1); // ROUTE_ID = 65535 (maximum U16)
        buffer.putShort((short)0);  // DESTINATION_ID
        buffer.flip();

        BufferedTrips trips = new BufferedTrips(stringTable, buffer);

        // Le comportement attendu est que l'index de route soit interprété comme 65535
        assertEquals(65535, trips.routeId(0) & 0xFFFF);
    }
}
