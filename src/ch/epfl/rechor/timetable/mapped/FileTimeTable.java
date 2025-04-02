package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static java.nio.file.Files.readAllLines;

public record FileTimeTable(Path directory,
                            List<String> stringTable,
                            Stations stations,
                            StationAliases stationAliases,
                            Platforms platforms,
                            Routes routes,
                            Transfers transfers) implements TimeTable {


    public static TimeTable in(Path directory) throws IOException {
        List<String> stringTable = List.copyOf(Files.readAllLines(directory.resolve("strings.txt"), StandardCharsets.ISO_8859_1));
        Stations stations;
        StationAliases stationAliases;
        Platforms platforms;
        Routes routes;
        Transfers transfers;

        try (FileChannel channel = FileChannel.open(directory.resolve("stations.bin"))) {
            ByteBuffer stationsBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            stations = new BufferedStations(stringTable, stationsBuffer);
        }
        try (FileChannel channel = FileChannel.open(directory.resolve("station-aliases.bin"))) {
            ByteBuffer stationsAliasesBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            stationAliases = new BufferedStationAliases(stringTable, stationsAliasesBuffer);
        }
        try (FileChannel channel = FileChannel.open(directory.resolve("platforms.bin"))) {
            ByteBuffer platformsBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            platforms = new BufferedPlatforms(stringTable, platformsBuffer);
        }
        try (FileChannel channel = FileChannel.open(directory.resolve("routes.bin"))) {
            ByteBuffer routesBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            routes = new BufferedRoutes(stringTable, routesBuffer);
        }
        try (FileChannel channel = FileChannel.open(directory.resolve("transfers.bin"))) {
            ByteBuffer transfersBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            transfers = new BufferedTransfers(transfersBuffer);
        }
        return new FileTimeTable(directory, stringTable, stations, stationAliases, platforms, routes, transfers);
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
        try  {
            Path dailyDirectory = directory.resolve(date.toString());
            FileChannel channel = FileChannel.open(dailyDirectory.resolve("trips.bin"));
            ByteBuffer tripsBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            Trips trips = new BufferedTrips(stringTable, tripsBuffer);
            return trips;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public Connections connectionsFor(LocalDate date) {

        try  {
            Path dailyDirectory = directory.resolve(date.toString());
            FileChannel channel1 = FileChannel.open(dailyDirectory.resolve("connections.bin"));
            FileChannel channel2 = FileChannel.open(dailyDirectory.resolve("connections-succ.bin"));
            ByteBuffer connectionsBuffer = channel1.map(FileChannel.MapMode.READ_ONLY, 0, channel1.size());
            ByteBuffer succConnectionsBuffer = channel2.map(FileChannel.MapMode.READ_ONLY, 0, channel2.size());
            Connections connections = new BufferedConnections(connectionsBuffer,succConnectionsBuffer);
            return connections;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}