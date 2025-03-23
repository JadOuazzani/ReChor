package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.Files.readAllLines;

public record FileTimeTable(Path directory,
                            List<String> stringTable,
                            Stations stations,
                            StationAliases stationAliases,
                            Platforms platforms, Routes routes,
                            Transfers transfers)
        implements TimeTable {

    public static TimeTable in(Path directory) throws IOException {
        Path strings = directory.resolve("strings.txt");
        Path stationAliases = directory.resolve("stations.bin");
        Path station = directory.resolve("station.bin");
        Path platforms = directory.resolve("platforms.bin");
        Path routes = directory.resolve("routes.bin");
        Path transfers = directory.resolve("transfers.bin");

        List<String> stringTable = List.copyOf(readAllLines(strings));
        ByteBuffer stationByteBuffer;
        ByteBuffer stationAliasesByteBuffer;
        ByteBuffer platformsByteBuffer;
        ByteBuffer routesByteBuffer;
        ByteBuffer transfersByteBuffer;

        try (FileChannel fileChannel = FileChannel.open(station)) {
            stationByteBuffer = fileChannel.map(READ_ONLY, 0, fileChannel.size());
        }
        try (FileChannel fileChannel = FileChannel.open(stationAliases)) {
            stationAliasesByteBuffer = fileChannel.map(READ_ONLY, 0, fileChannel.size());
        }
        try (FileChannel fileChannel = FileChannel.open(platforms)) {
            platformsByteBuffer = fileChannel.map(READ_ONLY, 0, fileChannel.size());
        }
        try (FileChannel fileChannel = FileChannel.open(routes)) {
            routesByteBuffer = fileChannel.map(READ_ONLY, 0, fileChannel.size());
        }
        try (FileChannel fileChannel = FileChannel.open(transfers)) {
            transfersByteBuffer = fileChannel.map(READ_ONLY, 0, fileChannel.size());
        }

        return new FileTimeTable(directory,
                stringTable,
                new BufferedStations(stringTable, stationByteBuffer),
                new BufferedStationAliases(stringTable, stationAliasesByteBuffer),
                new BufferedPlatforms(stringTable, platformsByteBuffer),
                new BufferedRoutes(stringTable, routesByteBuffer),
                new BufferedTransfers(transfersByteBuffer));
    }


    @Override
    public Stations stations() {
        return stations;
    }

    @Override
    public StationAliases stationAliases() {
        return stationAliases;
    }

    @Override
    public Platforms platforms() {
        return platforms;
    }

    @Override
    public Routes routes() {
        return routes;
    }

    @Override
    public Transfers transfers() {
        return transfers;
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        try {
            Path trips = Path.of(date.toString()).resolve("trips.bin");
            ByteBuffer tripsByteBuffer;
            try (FileChannel fileChannel = FileChannel.open(trips)) {
                tripsByteBuffer = fileChannel.map(READ_ONLY, 0, fileChannel.size());
            }
            return new BufferedTrips(stringTable(), tripsByteBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Connections connectionsFor(LocalDate date) {
        try {
            final Path datePath = directory().resolve(date.toString());
            Path connections = datePath.resolve("connections.bin");
            Path connectionsSucc = datePath.resolve("connections-succ.bin");
            ByteBuffer connectionsByteBuffer;
            ByteBuffer connectionsSuccByteBuffer;

            try (FileChannel fileChannel = FileChannel.open(connections)) {
                connectionsByteBuffer = fileChannel.map(READ_ONLY, 0, fileChannel.size());
            }
            try (FileChannel fileChannel = FileChannel.open(connectionsSucc)) {
                connectionsSuccByteBuffer = fileChannel.map(READ_ONLY, 0, fileChannel.size());
            }
            return new BufferedConnections(connectionsByteBuffer, connectionsSuccByteBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
