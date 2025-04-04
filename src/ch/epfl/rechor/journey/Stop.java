package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.util.Objects;

import static java.util.Objects.requireNonNull;


/**
 * Représente un arrêt avec un nom, une plateforme et des coordonnées géographiques.
 *
 * @param name         Le nom de l'arrêt, ne peut pas être nul.
 * @param platformName Le nom de la plateforme.
 * @param longitude    La longitude de l'arrêt, doit être comprise entre -180 et 180 degrés.
 * @param latitude     La latitude de l'arrêt, doit être comprise entre -90 et 90 degrés.
 */


public record Stop(String name, String platformName, double longitude, double latitude) {

    /**
     * Constructeur validant les contraintes des attributs.
     *
     * @throws NullPointerException     si {@code name} est nul.
     * @throws IllegalArgumentException si {@code latitude} ou {@code longitude} sont hors des bornes autorisées.
     */
    public Stop {
        Objects.requireNonNull(name);
        Preconditions.checkArgument(Math.abs(latitude) <= 90);
        Preconditions.checkArgument(Math.abs(longitude) <= 180);
    }


}

