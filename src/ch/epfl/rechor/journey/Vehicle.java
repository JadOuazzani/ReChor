package ch.epfl.rechor.journey;
import java.util.List;
/**
 * Enumération représentant les différents types de véhicules pouvant être utilisés dans un trajet.
 */
public enum Vehicle {
    /** Tramway */

    TRAM,
    /** Métro */

    METRO,
    /** Train */

    TRAIN,
    /** Bus */

    BUS,
    /** Ferry */

    FERRY,
    /** Remontée mécanique (télécabine, téléphérique, etc.) */

    AERIAL_LIFT,
    /** Funiculaire */

    FUNICULAR;


    /**
     * Liste immuable contenant tous les types de véhicules.
     */
    public static final List<Vehicle> ALL = List.of(Vehicle.values());

}
