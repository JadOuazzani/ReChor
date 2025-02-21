package ch.epfl.rechor.journey;
import ch.epfl.rechor.Preconditions;

import static java.util.Objects.requireNonNull;


public record Stop(String name, String platformName, double longitude, double latitude) {
    public Stop {
        requireNonNull(name);
        Preconditions.checkArgument(latitude<=90 &&latitude>=-90 && longitude<=180&&longitude>=-180);
    }
}
