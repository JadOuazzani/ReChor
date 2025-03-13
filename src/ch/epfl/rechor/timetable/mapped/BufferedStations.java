package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;

import java.nio.ByteBuffer;
import java.util.List;

public class BufferedStations implements Stations {

    BufferedStations(List<String> stringTable, ByteBuffer buffer){

    }

    @Override
    public String name(int id) throws IndexOutOfBoundsException {
        return "";
    }

    @Override
    public double longitude(int id) throws IndexOutOfBoundsException {
        return 0;
    }

    @Override
    public double latitude(int id) throws IndexOutOfBoundsException {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }
}
