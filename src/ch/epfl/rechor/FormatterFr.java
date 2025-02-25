package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;

public final class FormatterFr {

    public static String formatDuration(Duration duration) {
        int secondes = (int) duration.getSeconds();
        int minutes = (secondes / 60) % 60;
        int hours = secondes / 3600;
        if (hours == 0) {
            return minutes + " min";
        } else {
            return hours + " h " + minutes + " min";
        }
    }

    public static String formatTime(LocalDateTime dateTime) {
        int heures = dateTime.getHour();
        int minutes = dateTime.getMinute();
        String minute = "";
        if (minutes < 10) {
            minute = "0" + minutes;
        } else {
            minute = "" + minutes;
        }
        return heures + "h" + minute;
    }

    public static String formatPlatformName(Stop stop) {
        String platformName = stop.platformName();
        if (stop.platformName() == null || stop.platformName().isEmpty()) {
            return "";
        } else {
            char firstChar = platformName.charAt(0);

            if (Character.isDigit(firstChar)) {
                return "voie " + platformName;
            } else {
                return "quai " + platformName;
            }
        }
    }

    public static String formatLeg(Journey.Leg.Foot footLeg) {
        if (footLeg.isTransfer()) {
            return "changement " + "(" + formatDuration(footLeg.duration()) + ")";
        } else {
            return "trajet à pied " + "(" + formatDuration(footLeg.duration()) + ")";
        }
    }

    public static String formatLeg(Journey.Leg.Transport leg) {
        String depPlatformName;
        String arrPlatformName;
        if (leg.depStop().platformName() == null) {
            depPlatformName = "";
        } else {
            depPlatformName = " (" + formatPlatformName(leg.depStop()) + ")";
        }

        if (leg.arrStop().platformName() == null) {
            arrPlatformName = ")";
        } else {
            arrPlatformName = " " + formatPlatformName(leg.arrStop()) + ")";
        }

        return formatTime(leg.depTime()) + " " + leg.depStop().name() + depPlatformName + " → " + leg.arrStop().name() + " (arr. " + formatTime(leg.arrTime()) + arrPlatformName;

    }

    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        return transportLeg.route() + " Direction " + transportLeg.destination();

    }


}
