package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;

public final class FormatterFr {
    static String formatDuration(Duration duration) {
        int secondes = (int) duration.getSeconds();
        int minutes = (secondes/60)%60;
        int hourss = secondes/3600;
        if (hourss == 0) {
            return minutes+" min";
        }
        else {
            return hourss+" h "+ minutes+ " min";
        }
    }

    static String formatTime(LocalDateTime dateTime) {
        int heures = dateTime.getHour();
        int minutes = dateTime.getMinute();
        return heures+"h"+minutes;
    }

    static String formatPlatformName(Stop stop){
       String platformName = stop.platformName();
       char firstchar = platformName.charAt(0);

       if (Character.isDigit(firstchar)) {
           return "voie " + platformName;
       }

       else {
           return "quai " +platformName;
       }
    }

    static String formatLeg(Journey.Leg.Foot footLeg) {
        if (footLeg.isTransfer()) {
            return "changement " + formatDuration(footLeg.duration());
        }

        else {
            return "trajet à pied " + formatDuration(footLeg.duration());
        }
    }

    static String formatLeg(Journey.Leg.Transport leg) {
        if (formatPlatformName(leg.arrStop())==null);
    }





}
