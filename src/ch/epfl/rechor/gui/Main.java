package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.epfl.rechor.journey.JourneyExtractor.journeys;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        TimeTable timeTable = FileTimeTable.in(Path.of("timetable"));

        List<String> stops = new ArrayList<>();
        Map<String, String> altNames = new HashMap<>();

        for (int i = 0; i < timeTable.stations().size(); i++) {
            stops.add(timeTable.stations().name(i));
        }

        for (int i = 0; i < timeTable.stationAliases().size(); i++) {
            altNames.put(timeTable.stationAliases().alias(i), timeTable.stationAliases().stationName(i));
        }

        StopIndex stopIndex = new StopIndex(stops, altNames);

        QueryUI queryUI = QueryUI.create(stopIndex);

        ObservableValue<List<Journey>> journeysO = Bindings.createObjectBinding(() -> {
            String depStop = queryUI.depStopO().getValue();
            String arrStop = queryUI.arrStopO().getValue();
            LocalDate date = queryUI.dateO().getValue();
            if (date == null) return null;
            if (arrStop.isBlank() || depStop.isBlank()) return null;


            int depStationId = timeTable.stationId(stops.indexOf(depStop));
            int arrStationId = timeTable.stationId(stops.indexOf(arrStop));

            Router router = new Router(timeTable);
            Profile profile = router.profile(date, arrStationId);
            return journeys(profile, depStationId);
        }, queryUI.depStopO(), queryUI.arrStopO(), queryUI.dateO(), queryUI.timeO());

        SummaryUI summaryUI = SummaryUI.create(journeysO, queryUI.timeO());
        DetailUI detailUI = DetailUI.create(summaryUI.selectedJourneyO());

        SplitPane splitPane = new SplitPane(summaryUI.rootNode(), detailUI.rootNode());
        BorderPane root = new BorderPane();
        root.setTop(queryUI.rootNode());
        root.setCenter(splitPane);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        primaryStage.setTitle("ReCHor");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        Platform.runLater(() -> scene.lookup("#depStop").requestFocus());
    }
}
