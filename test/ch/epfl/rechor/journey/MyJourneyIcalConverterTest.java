package ch.epfl.rechor.journey;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

public class MyJourneyIcalConverterTest {

    @Test
    public void testToIcalendarFootLeg() {
        // Création de deux arrêts simples
        Stop depStop = new Stop("A", null, 0.0, 0.0);
        Stop arrStop = new Stop("B", null, 0.0, 0.0);
        LocalDateTime depTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime arrTime = LocalDateTime.of(2025, 1, 1, 10, 15);
        // Création d'une étape à pied
        Journey.Leg.Foot footLeg = new Journey.Leg.Foot(depStop, depTime, arrStop, arrTime);
        // Création d'un voyage avec une seule étape
        Journey journey = new Journey(List.of(footLeg));
        String ical = JourneyIcalConverter.toIcalendar(journey);

        // Vérifie la présence des composants et attributs requis
        assertTrue(ical.contains("BEGIN:VCALENDAR"));
        assertTrue(ical.contains("VERSION:2.0"));
        assertTrue(ical.contains("PRODID:ReCHor"));
        assertTrue(ical.contains("BEGIN:VEVENT"));
        assertTrue(ical.contains("UID:"));
        assertTrue(ical.contains("DTSTAMP:"));
        assertTrue(ical.contains("DTSTART:20250101T100000"));
        assertTrue(ical.contains("DTEND:20250101T101500"));
        assertTrue(ical.contains("SUMMARY:A → B"));
        assertTrue(ical.contains("trajet à pied (15 min)"));
        assertTrue(ical.contains("END:VEVENT"));
        assertTrue(ical.contains("END:VCALENDAR"));
    }


}
