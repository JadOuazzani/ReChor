package ch.epfl.rechor.journey;


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

/**
 * Convertit un voyage en un événement
 */

public final class JourneyIcalConverter {

    /**
     * Retourne une chaîne de caractères au format iCalendar représentant l'événement qui correspond au voyage donné
     */
    public static String toIcalendar(Journey journey) {

        IcalBuilder event = new IcalBuilder();

        event.begin(VCALENDAR)
                .add(VERSION, "2.0")
                .add(PRODID, "ReCHor")
                .begin(VEVENT) //composant imbriqué
                .add(UID, UUID.randomUUID().toString()) //la valeur est un UUID aléatoire
                .add(DTSTAMP, LocalDateTime.now()) //la valeur est la date/heure au moment de l'appel de la méthode
                .add(DTSTART, journey.depTime()) //la valeur est la date/heure du début du voyage
                .add(DTEND, journey.arrTime()) //la valeur est la date/heure de la fin du voyage
                .add(SUMMARY, journey.depStop().name() + " → " + journey.arrStop().name())
                .add(DESCRIPTION, valueDescription(journey))
                .end()
                .end();

        return event.build();
    }

    private static String valueDescription(Journey journey) {
        StringJoiner stringJoiner = new StringJoiner("\\n");

        for (Journey.Leg leg : journey.legs()) {
            switch (leg) { //filtrage de motifs (pattern-matching) dans un switch
                case Journey.Leg.Foot f -> stringJoiner.add(FormatterFr.formatLeg(f));
                case Journey.Leg.Transport t -> stringJoiner.add(FormatterFr.formatLeg(t));
            }
        }
        return stringJoiner.toString();
    }

}



