package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;

public interface Routes extends Indexed {

    abstract Vehicle vehicle(int id) throws IndexOutOfBoundsException;

    abstract String name(int id) throws IndexOutOfBoundsException;

}
