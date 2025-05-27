package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

/**
 * Provides cached JavaFX {@link Image} icons for each {@link Vehicle} type.
 * <p>
 * Icons are loaded on first request and stored in an internal cache so that
 * subsequent calls for the same {@code Vehicle} return the identical
 * {@code Image} instance, reducing memory usage and load time.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class VehicleIcons {
    private static final Map<Vehicle, Image> vehicleIcons = new EnumMap<>(Vehicle.class);

    private VehicleIcons() {
    }

    /**
     * Returns the {@link Image} icon corresponding to the given {@link Vehicle}.
     * <p>
     * If the icon has not yet been loaded, it is created by loading the image
     * resource named &lt;VEHICLE_NAME&gt;.png, where {@code VEHICLE_NAME} is
     * {@code vehicle.name()}, and then cached for future requests.
     * </p>
     *
     * @param vehicle the {@link Vehicle} whose icon is requested
     * @return the cached {@link Image} instance for the specified vehicle
     */
    public static Image iconFor(Vehicle vehicle) {
        return vehicleIcons.computeIfAbsent(vehicle, v -> new Image(v.name() + ".png"));
    }
}
