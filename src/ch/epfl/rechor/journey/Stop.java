package ch.epfl.rechor.journey;

public record Stop(String name, String platformName, double longitude, double latitude) {
    public Stop {
        if (name.equals(null)) {
            throw new NullPointerException();
        }
    }
}
