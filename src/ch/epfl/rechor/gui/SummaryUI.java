package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    public static SummaryUI create(ObservableValue<List<Journey>> journeyO, ObservableValue<LocalTime> timeO) {
        ListView<Journey> journeyListView = new ListView<>();
        journeyListView.getStylesheets().add("summary.css");

        ObservableList<Journey> items = FXCollections.observableArrayList();
        journeyListView.setItems(items);

        // Whenever the underlying list of journeys changes, reset the items
        // and re-apply the selection logic.
        journeyO.subscribe(list -> {
            if (list == null) {
                items.clear();
            } else {
                items.setAll(list);
                updateSelection(journeyListView, list, timeO.getValue());
            }
        });

        // Whenever the desired time changes, re-apply the selection logic
        // on the already‐populated 'items' list.
        timeO.subscribe(targetTime -> {
            List<Journey> currentList = journeyO.getValue();
            if (currentList != null && !currentList.isEmpty()) {
                updateSelection(journeyListView, currentList, targetTime);
            }
        });

        // cell factory and selection property remain unchanged
        journeyListView.setCellFactory(lv -> new cellJourney());
        ObservableValue<Journey> selected = journeyListView.getSelectionModel().selectedItemProperty();
        return new SummaryUI(journeyListView, selected);
    }

    private static void updateSelection(ListView<Journey> view,
                                        List<Journey> list,
                                        LocalTime targetTime) {
        if (list.isEmpty() || targetTime == null) {
            // nothing to select
            return;
        }
        int idx = 0;
        while (idx < list.size() &&
                list.get(idx).depTime().toLocalTime().isBefore(targetTime)) {
            idx++;
        }
        if (idx >= list.size()) {
            idx = list.size() - 1;
        }
        view.getSelectionModel().select(idx);
        view.scrollTo(idx);
    }

    private final static class cellJourney extends ListCell<Journey> {
        private final Text depTimeText = new Text();
        private final Text arrTimeText = new Text();
        private final Text durText = new Text();
        private final Text directionText = new Text();
        private final ImageView imageView = new ImageView();
        private final BorderPane cellRoot;
        private final Pane timeLinePane;
        private final Line track;
        private final Group circleGroup;

        cellJourney() {
            //top
            HBox hBoxTop = new HBox();
            hBoxTop.getStyleClass().add("route");
            hBoxTop.getChildren().addAll(imageView, directionText);

            //center
            track = new Line();
            circleGroup = new Group();
            timeLinePane = new Pane() {
                @Override
                protected void layoutChildren() {

                    double h = getHeight() / 2;
                    double margin = 5;
                    double w = getWidth() - margin - margin;

                    //1: positionner la ligne
                    Line track = (Line) getChildren().get(0);
                    track.setStartX(margin);
                    track.setEndX(getWidth() - margin);
                    track.setStartY(h);
                    track.setEndY(h);

                    //2: positionner chaque cercle

                    for (Node n : circleGroup.getChildren()) {
                        if (n instanceof Circle c) {
                            double ratio = (double) c.getUserData();
                            double x = margin + ratio * w;
                            c.setCenterX(x);
                            c.setCenterY(h);
                        }
                    }

                }
            };
            timeLinePane.setPrefSize(0, 0);
            timeLinePane.getChildren().addAll(track, circleGroup);


            //left
            depTimeText.getStyleClass().add("departure");

            //bottom
            HBox hBoxBottom = new HBox();
            hBoxBottom.getStyleClass().add("duration");
            hBoxBottom.getChildren().add(durText);

            cellRoot = new BorderPane(timeLinePane, hBoxTop, arrTimeText, hBoxBottom, depTimeText);
            cellRoot.getStyleClass().add("journey");
        }

        @Override
        protected void updateItem(Journey item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            List<Journey.Leg> legs = item.legs();

            if (legs.getFirst() instanceof Journey.Leg.Transport transportLeg) {
                imageView.setImage(VehicleIcons.iconFor(transportLeg.vehicle()));
                depTimeText.setText(FormatterFr.formatTime(item.depTime()));
                directionText.setText(FormatterFr.formatRouteDestination(transportLeg));
            } else if (legs.get(1) instanceof Journey.Leg.Transport transportLeg) {
                imageView.setImage(VehicleIcons.iconFor(transportLeg.vehicle()));
                depTimeText.setText(FormatterFr.formatTime(legs.get(1).depTime()));
                directionText.setText(FormatterFr.formatRouteDestination(transportLeg));
            }

            arrTimeText.setText(FormatterFr.formatTime(item.arrTime()));
            durText.setText(FormatterFr.formatDuration(item.duration()));
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            circleGroup.getChildren().clear();
            double totalDuration = item.duration().toSeconds();

            Circle startDot = new Circle(3);
            startDot.getStyleClass().add("dep-arr");
            startDot.setUserData(0.0);
            circleGroup.getChildren().add(startDot);
            for (Journey.Leg leg : item.legs()) {
                Circle dot = new Circle(3);
                double ratio = 0;
                if (leg instanceof Journey.Leg.Foot) {
                    ratio = Duration.between(item.depTime(), leg.depTime()).toSeconds() / totalDuration;
                    dot.getStyleClass().add("transfer");
                    dot.setUserData(ratio);
                    circleGroup.getChildren().add(dot);
                }
            }
            Circle endDot = new Circle(3);
            endDot.getStyleClass().add("dep-arr");
            endDot.setUserData(1.0);
            circleGroup.getChildren().add(endDot);
            setGraphic(cellRoot);

        }
    }
}