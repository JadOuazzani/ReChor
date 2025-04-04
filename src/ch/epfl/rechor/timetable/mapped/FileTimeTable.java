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

/**
 * Represents a public transportation timetable whose flattened data is stored in files.
 * <p>
 * This class implements the {@link TimeTable} interface and provides a static method
 * to create an instance from a directory containing timetable data files. The data includes
 * a string table, stations, station aliases, platforms, routes, and transfers.
 * </p>
 *
 * @param directory      the path to the directory containing the timetable data files.
 * @param stringTable    the table of strings.
 * @param stations       the stations.
 * @param stationAliases the station aliases.
 * @param platforms      the platforms.
 * @param routes         the routes.
 * @param transfers      the transfers.
 */
public record FileTimeTable(Path directory,
                            List<String> stringTable,
                            Stations stations,
                            StationAliases stationAliases,
                            Platforms platforms,
                            Routes routes,
                            Transfers transfers) implements TimeTable {

    /**
     * Creates an instance of {@code FileTimeTable} from the directory containing the timetable data files.
     * <p>
     * This method reads the following files from the given directory:
     * <ul>
     *   <li>strings.txt</li>
     *   <li>stations.bin</li>
     *   <li>station-aliases.bin</li>
     *   <li>platforms.bin</li>
     *   <li>routes.bin</li>
     *   <li>transfers.bin</li>
     * </ul>
     * and builds the corresponding objects.
     * </p>
     *
     * @param directory the path to the directory containing the data.
     * @return a new instance of {@code FileTimeTable} initialized with the read data.
     * @throws IOException if an I/O error occurs during file reading.
     */
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

    /**
     * {@inheritDoc}
     *
     * @return the stations.
     */
    @Override
    public Stations stations() {
        return stations;
    }

    /**
     * {@inheritDoc}
     *
     * @return the station aliases.
     */
    @Override
    public StationAliases stationAliases() {
        return stationAliases;
    }

    /**
     * {@inheritDoc}
     *
     * @return the platforms.
     */
    @Override
    public Platforms platforms() {
        return platforms;
    }

    /**
     * {@inheritDoc}
     *
     * @return the routes.
     */
    @Override
    public Routes routes() {
        return routes;
    }

    /**
     * {@inheritDoc}
     *
     * @return the transfers.
     */
    @Override
    public Transfers transfers() {
        return transfers;
    }

    /**
     * Returns the trips for a given date.
     * <p>
     * This method reads the {@code trips.bin} file located in the subdirectory corresponding
     * to the specified date. If an I/O error occurs, an {@link UncheckedIOException} is thrown.
     * </p>
     *
     * @param date the date for which trips are requested.
     * @return the trips for the specified date.
     * @throws UncheckedIOException if an I/O error occurs while reading the file.
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        try {
            Path dailyDirectory = directory.resolve(date.toString());
            FileChannel channel = FileChannel.open(dailyDirectory.resolve("trips.bin"));
            ByteBuffer tripsBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            Trips trips = new BufferedTrips(stringTable, tripsBuffer);
            return trips;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns the connections for a given date.
     * <p>
     * This method reads the {@code connections.bin} and {@code connections-succ.bin} files located in the subdirectory
     * corresponding to the specified date. If an I/O error occurs, an {@link UncheckedIOException} is thrown.
     * </p>
     *
     * @param date the date for which connections are requested.
     * @return the connections for the specified date.
     * @throws UncheckedIOException if an I/O error occurs while reading the files.
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        try {
            Path dailyDirectory = directory.resolve(date.toString());
            FileChannel channel1 = FileChannel.open(dailyDirectory.resolve("connections.bin"));
            FileChannel channel2 = FileChannel.open(dailyDirectory.resolve("connections-succ.bin"));
            ByteBuffer connectionsBuffer = channel1.map(FileChannel.MapMode.READ_ONLY, 0, channel1.size());
            ByteBuffer succConnectionsBuffer = channel2.map(FileChannel.MapMode.READ_ONLY, 0, channel2.size());
            Connections connections = new BufferedConnections(connectionsBuffer, succConnectionsBuffer);
            return connections;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
