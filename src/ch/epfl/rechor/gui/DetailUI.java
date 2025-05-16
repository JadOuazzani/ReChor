package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static javafx.scene.paint.Color.RED;

public record DetailUI(Node rootNode) {

    public static DetailUI create(ObservableValue<Journey> observableJourney) throws URISyntaxException {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStylesheets().add("detail.css");
        scrollPane.setId("detail");

        StackPane stackPane = new StackPane();
        scrollPane.setContent(stackPane);

        //Aucun voyage
        VBox noJourneyBox = new VBox();
        stackPane.getChildren().add(noJourneyBox);
        noJourneyBox.setId("no-journey");
        noJourneyBox.getChildren().add(new Text("Aucun voyage"));

        VBox journeyBox = new VBox();
        stackPane.getChildren().add(journeyBox);

        StackPane stackPane1 = new StackPane();
        journeyBox.getChildren().add(stackPane1);

        //Annotations
        Pane annotationPane = new Pane();
        annotationPane.setId("annotations");
        annotationPane.setMouseTransparent(true);

        boolean hasJourney = observableJourney.getValue() != null && !observableJourney.getValue().legs().isEmpty();
        toggleVisibility(noJourneyBox, hasJourney, journeyBox);


        //Etapes
        GridPane gridPane = new LegsGrid(annotationPane);
        gridPane.setId("legs");

        stackPane1.getChildren().add(new StackPane(gridPane, annotationPane));


        HBox hBox = new HBox();
        hBox.setId("buttons");
        journeyBox.getChildren().add(hBox);

        //Carte
        Button carteButton = new Button();
        carteButton.setText("Carte");
        hBox.getChildren().add(carteButton);
        carteButton.setOnAction(event -> {
            try {
                URI uri = new URI("https", "umap.osm.ch", "/fr/map", "data=" + JourneyGeoJsonConverter.toGeoJson(observableJourney.getValue()), null);
                Desktop.getDesktop().browse(uri);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });

        //calendrier
        Button buttonCalendar = new Button();
        buttonCalendar.setText("Calendrier");
        hBox.getChildren().add(buttonCalendar);
        buttonCalendar.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            String dateStr = observableJourney.getValue().depTime().toLocalDate().toString();

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("fichier icalendar", "*.ics"));
            fileChooser.setInitialFileName("journey_" + dateStr + ".ics");
            File file = fileChooser.showSaveDialog(buttonCalendar.getScene().getWindow());
            if (file != null) {
                try {
                    Files.writeString(file.toPath(), JourneyIcalConverter.toIcalendar(observableJourney.getValue()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });


        observableJourney.subscribe(journey -> {
            boolean hasNewJourney = journey != null;
            toggleVisibility(noJourneyBox, hasNewJourney, journeyBox);

            if (hasNewJourney) {
                gridPane.getChildren().clear();
                annotationPane.getChildren().clear();
                buildJourneyUI(observableJourney, gridPane);
            }
        });

        return new DetailUI(scrollPane);
    }

    private static void buildJourneyUI(ObservableValue<Journey> observableJourney, GridPane gridPane) {
        ((LegsGrid) gridPane).clearAnnotations();
        int row = 0;
        for (Journey.Leg leg : observableJourney.getValue().legs()) {
            if (leg instanceof Journey.Leg.Foot) {
                Text text = new Text(FormatterFr.formatLeg((Journey.Leg.Foot) leg));
                GridPane.setColumnSpan(text, 2);
                gridPane.add(text, 2, row);
                row++;
            } else if (leg instanceof Journey.Leg.Transport transportLeg) {
                //1:Depart
                Label depTime = new Label(FormatterFr.formatTime(leg.depTime()));
                depTime.getStyleClass().add("departure");
                gridPane.add(depTime, 0, row); // heure de depart

                Circle depCircle = new Circle(3); // cercle
                gridPane.add(depCircle, 1, row);

                gridPane.add(new Label(leg.depStop().name()), 2, row); // nom de la gare de depart
                Label depPlatformName = new Label(leg.depStop().platformName());
                depPlatformName.getStyleClass().add("departure");
                gridPane.add(depPlatformName, 3, row);//nom de la voie/quaie

                row++;

                //2: vehicule
                ImageView vehicleIcon = new ImageView(VehicleIcons.iconFor(transportLeg.vehicle())); //icone du vehicule
                vehicleIcon.setFitHeight(31);
                vehicleIcon.setFitWidth(31);
                gridPane.add(vehicleIcon, 0, row);

                Label nameDestination = new Label(FormatterFr.formatRouteDestination(transportLeg)); //nom de la destination
                GridPane.setColumnSpan(nameDestination, 2);
                gridPane.add(nameDestination, 2, row);
                row++;

                //3:intermediate stop
                if (!leg.intermediateStops().isEmpty()) { //continent un arret intermediare
                    GridPane.setRowSpan(vehicleIcon, 2);
                    String intermediateStopSize = leg.intermediateStops().size() + " arrêts, " + FormatterFr.formatDuration(leg.duration());
                    GridPane intermediateGridPane = new GridPane();
                    intermediateGridPane.getStyleClass().add("intermediate-stops");

                    int intermediateRow = 0;
                    for (Journey.Leg.IntermediateStop stop : leg.intermediateStops()) {
                        intermediateGridPane.add(new Label(FormatterFr.formatTime(stop.depTime())), 0, intermediateRow);
                        intermediateGridPane.add(new Label(FormatterFr.formatTime(stop.arrTime())), 1, intermediateRow);
                        intermediateGridPane.add(new Label(stop.stop().name()), 2, intermediateRow);
                        intermediateRow++;
                    }

                    TitledPane titledPane = new TitledPane(intermediateStopSize, intermediateGridPane);
                    Accordion accordion = new Accordion(titledPane);
                    GridPane.setColumnSpan(accordion, 2);
                    gridPane.add(accordion, 2, row);
                    row++;
                }

                //4:arriving
                gridPane.add(new Label(FormatterFr.formatTime(leg.arrTime())), 0, row); // heure d'arrivée

                Circle arrCircle = new Circle(3); // cercle
                gridPane.add(arrCircle, 1, row);

                gridPane.add(new Label(leg.arrStop().name()), 2, row); // nom de la gare d'arrivée
                gridPane.add(new Label(leg.arrStop().platformName()), 3, row);//nom de la voie/quaie
                row++;

                ((LegsGrid) gridPane).addAnnotation(depCircle, arrCircle);

            }

        }
    }

    private static void toggleVisibility(VBox noJourneyBox, boolean hasJourney, VBox journeyVBox) {
        noJourneyBox.setVisible(!hasJourney);
        noJourneyBox.setManaged(!hasJourney);
        journeyVBox.setVisible(hasJourney);
        journeyVBox.setManaged(hasJourney);
    }

    private static final class LegsGrid extends GridPane {
        private final List<Pair<Circle, Circle>> links = new ArrayList<>();
        private final Pane annotationPane;

        LegsGrid(Pane annotationPane) {
            this.annotationPane = annotationPane;
        }

        void addAnnotation(Circle a, Circle b) {
            links.add(new Pair<>(a, b));
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            List<Line> lines = new ArrayList<>();
            for (var p : links) {
                Bounds A = p.getKey().getBoundsInParent();
                Bounds B = p.getValue().getBoundsInParent();
                double x1 = A.getCenterX();
                double y1 = A.getCenterY();
                double x2 = B.getCenterX();
                double y2 = B.getCenterY();
                Line l = new Line(x1, y1, x2, y2);
                l.setStrokeWidth(2);
                l.setStroke(RED);
                lines.add(l);
            }

            // on met à jour le Pane d'annotations en une seule fois
            annotationPane.getChildren().setAll(lines);
        }

        void clearAnnotations() {
            links.clear();
            annotationPane.getChildren().clear();
        }

    }

}


