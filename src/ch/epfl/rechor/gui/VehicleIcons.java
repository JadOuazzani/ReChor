package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import javafx.scene.image.Image;

public class VehicleIcons {
    private static final Map<Vehicle, Image> vehicleIcons = new EnumMap<>(Vehicle.class);

    private VehicleIcons() {}

    public static Image iconFor (Vehicle vehicle) {
        return vehicleIcons.computeIfAbsent (vehicle, v -> {
            return new Image(v.name() + ".png");
        });
    }




}
