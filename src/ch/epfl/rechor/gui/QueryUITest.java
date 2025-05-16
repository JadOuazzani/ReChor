package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.application.Application.launch;

public final class QueryUITest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        TimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable")));
        List<String> stationNames = new ArrayList<>();
        for (int i = 0; i < timeTable.stations().size(); i++) {
            stationNames.add(timeTable.stations().name(i));
        }
        Map<String, String> aliases = new HashMap<>();
        var stationAliases = timeTable.stationAliases();
        for (int i = 0; i < stationAliases.size(); i++) {
            aliases.put(stationAliases.alias(i), stationAliases.stationName(i));
        }
        StopIndex stopIndex = new StopIndex(stationNames, aliases);
        QueryUI queryUI = QueryUI.create(stopIndex);
        
        BorderPane root = new BorderPane(queryUI.rootNode());
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }
} 