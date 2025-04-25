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
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
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
        List<String> stringTable = List.copyOf(
                Files.readAllLines(directory.resolve("strings.txt"), StandardCharsets.ISO_8859_1));
        Stations stations = new BufferedStations(stringTable, map(directory, "stations.bin"));
        StationAliases stationAliases = new BufferedStationAliases(
                stringTable,
                map(directory, "station-aliases.bin"));
        Platforms platforms = new BufferedPlatforms(stringTable, map(directory, "platforms.bin"));
        Routes routes = new BufferedRoutes(stringTable, map(directory, "routes.bin"));
        Transfers transfers = new BufferedTransfers(map(directory, "transfers.bin"));

        return new FileTimeTable(directory, stringTable, stations, stationAliases, platforms, routes, transfers);
    }

    /**
     * Maps a file located in the specified directory into a read-only {@link ByteBuffer}.
     * <p>
     * This auxiliary method opens the file specified by its filename in the given directory,
     * maps its entire content into memory as a read-only buffer, and returns the resulting {@code ByteBuffer}.
     * The file channel is automatically closed after mapping.
     * </p>
     *
     * @param directory the directory containing the file.
     * @param filename  the name of the file to map.
     * @return a read-only {@link ByteBuffer} representing the content of the file.
     * @throws IOException if an I/O error occurs while opening or mapping the file.
     */
    private static ByteBuffer map(Path directory, String filename) throws IOException {
        try (FileChannel channel = FileChannel.open(directory.resolve(filename))) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
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
            return new BufferedTrips(stringTable, tripsBuffer);
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
            return new BufferedConnections(connectionsBuffer, succConnectionsBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
