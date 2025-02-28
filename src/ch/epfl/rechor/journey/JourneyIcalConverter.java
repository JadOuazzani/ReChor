package ch.epfl.rechor.journey;


/*
 *	Author:      Jad Ouazzani
 *	Date:
 */

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.StringJoiner;
import java.util.UUID;

import static ch.epfl.rechor.IcalBuilder.Component.VCALENDAR;
import static ch.epfl.rechor.IcalBuilder.Component.VEVENT;
import static ch.epfl.rechor.IcalBuilder.Name.*;


public final class JourneyIcalConverter {

    public static String toIcalendar(Journey journey) {

        IcalBuilder event = new IcalBuilder();

        event.begin(VCALENDAR)
                .add(VERSION, "2.0")
                .add(PRODID, "ReCHor")
                .begin(VEVENT).add(UID, UUID.randomUUID().toString())
                .add(DTSTAMP, LocalDateTime.now())
                .add(DTEND, journey.arrTime())
                .add(SUMMARY, journey.arrStop().name() + " → " + journey.depStop().name())
                .add(DESCRIPTION, valueDescription(journey))
                .end()
                .end();

        return event.build();
    }

    private static String valueDescription(Journey journey) {
        StringJoiner stringJoiner = new StringJoiner("\n");

        for (Journey.Leg leg : journey.legs()) {
            switch (leg) {
                case Journey.Leg.Foot f -> stringJoiner.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t -> stringJoiner.add(FormatterFr.formatLeg(t));
            }
        }
        return stringJoiner.toString();
    }

}



