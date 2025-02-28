package ch.epfl.rechor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class MyIcalBuilderTest {

    @Test
    public void testAddStringFolding() {
        IcalBuilder builder = new IcalBuilder();
        // Valeur longue pour forcer le pliage (>75 caractères)
        String longValue = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        builder.add(IcalBuilder.Name.DESCRIPTION, longValue);
        String result = builder.build();
        // Vérifie que la ligne a été pliée (doit contenir "\r\n ")
        assertTrue(result.contains("\r\n "));
    }

    @Test
    public void testAddLocalDateTime() {
        IcalBuilder builder = new IcalBuilder();
        LocalDateTime dt = LocalDateTime.of(2025, 2, 21, 12, 30, 45);
        builder.add(IcalBuilder.Name.DTSTAMP, dt);
        String result = builder.build();
        // Format attendu "yyyyMMdd'T'HHmmss", par exemple "20250221T123045"
        assertTrue(result.contains("20250221T123045"));
    }

    @Test
    public void testBeginEndAndBuild() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VCALENDAR)
                .add(IcalBuilder.Name.PRODID, "TestProd")
                .begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.SUMMARY, "TestEvent")
                .end()   // Termine VEVENT
                .end();  // Termine VCALENDAR
        String result = builder.build();
        assertTrue(result.contains("BEGIN:VCALENDAR"));
        assertTrue(result.contains("BEGIN:VEVENT"));
        assertTrue(result.contains("END:VEVENT"));
        assertTrue(result.contains("END:VCALENDAR"));
    }

    @Test
    public void testEndWithoutBegin() {
        IcalBuilder builder = new IcalBuilder();
        assertThrows(IllegalArgumentException.class, builder::end);
    }

    @Test
    public void testBuildWithOpenComponent() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VCALENDAR);
        assertThrows(IllegalArgumentException.class, builder::build);
    }
}
