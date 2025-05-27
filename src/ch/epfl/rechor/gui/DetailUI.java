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

/**
 * Represents the detailed view of a selected journey in the ReCHor GUI.
 * <p>
 * Provides a scrollable pane showing either a "No journey" message or the
 * sequence of legs for the current {@link Journey}, connected by
 * red lines between departure and arrival circles. Includes buttons to
 * display the route on a map and to export the journey as an iCalendar file.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 *
 * @param rootNode the root JavaFX {@link Node} of this detailed view
 */
public record DetailUI(Node rootNode) {

    /**
     * Creates and initializes the detail view for a journey.
     * <p>
     * Builds a scrollable layout containing:
     * <ul>
     *   <li>A "No journey" placeholder when no journey is present.</li>
     *   <li>A grid of legs annotated with circles and connecting lines.</li>
     *   <li>Buttons labeled "Carte" (map) and "Calendrier" (calendar),
     *       which open the journey's GeoJSON on uMap and export it as .ics.</li>
     * </ul>
     * The view observes changes in the supplied {@code observableJourney}
     * and updates its contents accordingly.
     * </p>
     *
     * @param observableJourney an {@link ObservableValue} wrapping the currently
     *                          selected {@link Journey}; may be null initially
     * @return a new {@code DetailUI} instance containing the constructed UI
     * @throws URISyntaxException if building the map URI fails
     */
    public static DetailUI create(ObservableValue<Journey> observableJourney) throws URISyntaxException {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStylesheets().add("detail.css");
        scrollPane.setId("detail");

        StackPane stackPane = new StackPane();
        scrollPane.setContent(stackPane);

        // "No journey" placeholder
        VBox noJourneyBox = new VBox();
        stackPane.getChildren().add(noJourneyBox);
        noJourneyBox.setId("no-journey");
        noJourneyBox.getChildren().add(new Text("Aucun voyage"));

        // Container for actual journey details
        VBox journeyBox = new VBox();
        stackPane.getChildren().add(journeyBox);

        StackPane stackPane1 = new StackPane();
        journeyBox.getChildren().add(stackPane1);

        // Pane for annotation lines between circles
        Pane annotationPane = new Pane();
        annotationPane.setId("annotations");
        annotationPane.setMouseTransparent(true);

        boolean hasJourney = observableJourney.getValue() != null && !observableJourney.getValue().legs().isEmpty();
        toggleVisibility(noJourneyBox, hasJourney, journeyBox);

        // Grid of journey legs
        GridPane gridPane = new LegsGrid(annotationPane);
        gridPane.setId("legs");
        stackPane1.getChildren().add(new StackPane(annotationPane, gridPane));

        // Buttons for map and calendar export
        HBox hBox = new HBox();
        hBox.setId("buttons");
        journeyBox.getChildren().add(hBox);

        // Map button
        Button carteButton = new Button("Carte");
        hBox.getChildren().add(carteButton);
        carteButton.setOnAction(event -> {
            try {
                URI uri = new URI(
                        "https",
                        "umap.osm.ch",
                        "/fr/map",
                        "data=" + JourneyGeoJsonConverter.toGeoJson(observableJourney.getValue()),
                        null
                );
                Desktop.getDesktop().browse(uri);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });

        // Calendar export button
        Button buttonCalendar = new Button("Calendrier");
        hBox.getChildren().add(buttonCalendar);
        buttonCalendar.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            String dateStr = observableJourney.getValue().depTime().toLocalDate().toString();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("fichier icalendar", "*.ics")
            );
            fileChooser.setInitialFileName("journey_" + dateStr + ".ics");
            File file = fileChooser.showSaveDialog(buttonCalendar.getScene().getWindow());
            if (file != null) {
                try {
                    Files.writeString(
                            file.toPath(),
                            JourneyIcalConverter.toIcalendar(observableJourney.getValue())
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Update view when the observed journey changes
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

    /**
     * Populates the {@link GridPane} with rows representing each leg of the journey.
     * <p>
     * For each foot leg, adds a single text row. For each transport leg:
     * <ol>
     *   <li>Departure time, circle, station name, and platform (styled as departure).</li>
     *   <li>Vehicle icon and route destination (spanning two columns).</li>
     *   <li>An optional {@link Accordion} for intermediate stops, if any.</li>
     *   <li>Arrival time, circle, station name, and platform.</li>
     *   <li>Registers the departure & arrival circles with the {@code LegsGrid}
     *       for line annotation.</li>
     * </ol>
     * </p>
     *
     * @param observableJourney the observable journey value
     * @param gridPane          the {@code GridPane} to populate
     */
    private static void buildJourneyUI(ObservableValue<Journey> observableJourney, GridPane gridPane) {
        ((LegsGrid) gridPane).clearAnnotations();
        int row = 0;
        for (Journey.Leg leg : observableJourney.getValue().legs()) {
            if (leg instanceof Journey.Leg.Foot) {
                Text text = new Text(FormatterFr.formatLeg((Journey.Leg.Foot) leg));
                GridPane.setColumnSpan(text, 2);
                gridPane.add(text, 2, row++);
            } else if (leg instanceof Journey.Leg.Transport transportLeg) {
                // 1: Departure row
                Label depTime = new Label(FormatterFr.formatTime(leg.depTime()));
                depTime.getStyleClass().add("departure");
                gridPane.add(depTime, 0, row);
                Circle depCircle = new Circle(3);
                gridPane.add(depCircle, 1, row);
                gridPane.add(new Label(leg.depStop().name()), 2, row);
                Label depPlatform = new Label(leg.depStop().platformName());
                depPlatform.getStyleClass().add("departure");
                gridPane.add(depPlatform, 3, row++);
                // 2: Vehicle row
                ImageView icon = new ImageView(VehicleIcons.iconFor(transportLeg.vehicle()));
                icon.setFitHeight(31);
                icon.setFitWidth(31);
                gridPane.add(icon, 0, row);
                Label dest = new Label(FormatterFr.formatRouteDestination(transportLeg));
                GridPane.setColumnSpan(dest, 2);
                gridPane.add(dest, 2, row++);
                // 3: Intermediate stops accordion
                if (!leg.intermediateStops().isEmpty()) {
                    GridPane.setRowSpan(icon, 2);
                    String sizeLabel = leg.intermediateStops().size()
                            + " arrêts, "
                            + FormatterFr.formatDuration(leg.duration());
                    GridPane intermediate = new GridPane();
                    intermediate.getStyleClass().add("intermediate-stops");
                    int subRow = 0;
                    for (Journey.Leg.IntermediateStop stop : leg.intermediateStops()) {
                        intermediate.add(new Label(FormatterFr.formatTime(stop.arrTime())), 0, subRow);
                        intermediate.add(new Label(FormatterFr.formatTime(stop.depTime())), 1, subRow);
                        intermediate.add(new Label(stop.stop().name()), 2, subRow++);
                    }
                    TitledPane tp = new TitledPane(sizeLabel, intermediate);
                    Accordion acc = new Accordion(tp);
                    GridPane.setColumnSpan(acc, 2);
                    gridPane.add(acc, 2, row++);
                }
                // 4: Arrival row
                gridPane.add(new Label(FormatterFr.formatTime(leg.arrTime())), 0, row);
                Circle arrCircle = new Circle(3);
                gridPane.add(arrCircle, 1, row);
                gridPane.add(new Label(leg.arrStop().name()), 2, row);
                gridPane.add(new Label(leg.arrStop().platformName()), 3, row++);
                // Register circles for annotation lines
                ((LegsGrid) gridPane).addAnnotation(depCircle, arrCircle);
            }
        }
    }

    /**
     * Toggles visibility and management of the "no journey" placeholder and the
     * journey detail container based on whether a journey is present.
     *
     * @param noJourneyBox the container showing "Aucun voyage"
     * @param hasJourney   {@code true} if a journey is present
     * @param journeyVBox  the container for journey details
     */
    private static void toggleVisibility(VBox noJourneyBox, boolean hasJourney, VBox journeyVBox) {
        noJourneyBox.setVisible(!hasJourney);
        noJourneyBox.setManaged(!hasJourney);
        journeyVBox.setVisible(hasJourney);
        journeyVBox.setManaged(hasJourney);
    }

    /**
     * A specialized {@link GridPane} that tracks pairs of departure/arrival circles
     * and draws red lines between them after layout.
     */
    private static final class LegsGrid extends GridPane {
        private final List<Pair<Circle, Circle>> links = new ArrayList<>();
        private final Pane annotationPane;

        /**
         * Constructs a new {@code LegsGrid} that will render lines into the given pane.
         *
         * @param annotationPane the {@link Pane} where connecting lines are drawn
         */
        LegsGrid(Pane annotationPane) {
            this.annotationPane = annotationPane;
        }

        /**
         * Registers a pair of circles to be connected by a line.
         *
         * @param a the departure circle
         * @param b the arrival circle
         */
        void addAnnotation(Circle a, Circle b) {
            links.add(new Pair<>(a, b));
        }

        /**
         * After laying out children, computes and draws red lines connecting
         * each registered circle pair in the annotation pane.
         */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            List<Line> lines = new ArrayList<>();
            for (var pair : links) {
                Bounds A = pair.getKey().getBoundsInParent();
                Bounds B = pair.getValue().getBoundsInParent();
                Line line = new Line(
                        A.getCenterX(), A.getCenterY(),
                        B.getCenterX(), B.getCenterY()
                );
                line.setStrokeWidth(2);
                line.setStroke(RED);
                lines.add(line);
            }
            annotationPane.getChildren().setAll(lines);
        }

        /**
         * Clears all registered circle pairs and removes any drawn lines.
         */
        void clearAnnotations() {
            links.clear();
            annotationPane.getChildren().clear();
        }
    }

}
