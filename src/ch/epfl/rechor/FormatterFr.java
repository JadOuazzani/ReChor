package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.StringJoiner;

/**
 * La représentation textuelle de différents types de données
 */

public final class FormatterFr {

    /**
     * nombre d'heures et de minutes, ou de minutes seulement pour les durées inférieures à une heure
     */
    public static String formatDuration(Duration duration) {
        int secondes = (int) duration.getSeconds();
        int minutes = (secondes / 60) % 60;
        int hours = secondes / 3600;
        return hours == 0 ? minutes + " min" : hours + " h " + minutes + " min";
    }

    /**
     * Une heure est représentée textuellement en séparant par la lettre h
     */
    public static String formatTime(LocalDateTime dateTime) {
        int heures = dateTime.getHour();
        int minutes = dateTime.getMinute();
        String minute = minutes < 10 ? "0" + minutes : "" + minutes;
        return heures + "h" + minute;
    }

    /**
     * Les voies de chemin de fer soient identifiées par des nombres, tandis que les quais des gares routières le soient par des lettres.
     */
    public static String formatPlatformName(Stop stop) {
        String platformName = stop.platformName();
        return (platformName == null || platformName.isEmpty()) ? "" :
                (Character.isDigit(platformName.charAt(0)) ? "voie " + platformName : "quai " + platformName);
    }

    /**
     * Une étape à pied est représentée par une description de son type et sa durée
     */
    public static String formatLeg(Journey.Leg.Foot footLeg) {
        String legDuration = formatDuration(footLeg.duration());
        return footLeg.isTransfer() ? "changement " + "(" + legDuration + ")" : "trajet à pied " + "(" + formatDuration(footLeg.duration()) + ")";
    }

    /**
     * Representation textuelle d'une étape en transport public
     */
    public static String formatLeg(Journey.Leg.Transport leg) {
        String depPlatformName = leg.depStop().platformName() == null ? "" : " (" + formatPlatformName(leg.depStop()) + ")";
        String arrPlatformName = leg.arrStop().platformName() == null ? ")" : " " + formatPlatformName(leg.arrStop()) + ")";
        return new StringBuilder()
                .append(formatTime(leg.depTime()))
                .append(" ")
                .append(leg.depStop().name())
                .append(depPlatformName)
                .append(" → ")
                .append(leg.arrStop().name())
                .append(" (arr. ")
                .append(formatTime(leg.arrTime()))
                .append(arrPlatformName)
                .toString();
    }

    /**
     * Representation textuelle du véhicule qui parcourt une ligne dans un sens donné
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        return transportLeg.route() + " Direction " + transportLeg.destination();
    }


}
