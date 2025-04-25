package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

import static ch.epfl.rechor.IcalBuilder.Component.VCALENDAR;
import static ch.epfl.rechor.IcalBuilder.Component.VEVENT;
import static ch.epfl.rechor.IcalBuilder.Name.*;

/**
 * Converts a journey into an iCalendar event.
 */
public final class JourneyIcalConverter {

    private static final String ICALENDAR_VERSION = "2.0";
    private static final String PROD_ID = "ReCHor";
    private static final String SUMMARY_ARROW = " → ";
    private static final String NEWLINE_DELIMITER = "\\n";

    /**
     * Returns a string in iCalendar format representing the event corresponding
     * to the given journey.
     *
     * @param journey the journey to be converted into an iCalendar event.
     * @return the iCalendar formatted string representing the event.
     */
    public static String toIcalendar(Journey journey) {
        IcalBuilder event = new IcalBuilder();
        String summary = journey.depStop().name() + SUMMARY_ARROW
                + journey.arrStop().name();

        event.begin(VCALENDAR)
                .add(VERSION, ICALENDAR_VERSION)
                .add(PRODID, PROD_ID)
                .begin(VEVENT) // nested component
                .add(UID, UUID.randomUUID().toString()) // random UUID
                .add(DTSTAMP, LocalDateTime.now()) // current date/time
                .add(DTSTART, journey.depTime()) // journey departure
                .add(DTEND, journey.arrTime())   // journey arrival
                .add(SUMMARY, summary)
                .add(DESCRIPTION, valueDescription(journey))
                .end()
                .end();

        return event.build();
    }

    /**
     * Generates the description for the iCalendar event based on the journey's legs.
     *
     * @param journey the journey from which the description is generated.
     * @return a string representing the description, with each leg's details
     *         separated by newline characters.
     */
    private static String valueDescription(Journey journey) {
        StringJoiner joiner = new StringJoiner(NEWLINE_DELIMITER);

        for (Journey.Leg leg : journey.legs()) {
            switch (leg) { // pattern matching switch
                case Journey.Leg.Foot f ->
                        joiner.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t ->
                        joiner.add(FormatterFr.formatLeg(t));
            }
        }
        return joiner.toString();
    }
}
