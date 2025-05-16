package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record QueryUI(Node rootNode, ObservableValue<String> depStopO, ObservableValue<String> arrStopO, ObservableValue<LocalDate> dateO, ObservableValue<LocalTime> timeO) {
    public static QueryUI create(StopIndex stopIndex) {
        Label depStopLabel = new Label("Départ\u202f:");
        Label arrStopLabel = new Label("Arrivée\u202f:");
        Label dateLabel = new Label("Date\u202f:");
        Label timeLabel = new Label("Heure\u202f:");
        StopField depStopField = StopField.create(stopIndex);
        StopField arrStopField = StopField.create(stopIndex);
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField timeField = new TextField();
        timeField.setPromptText("HH:MM");
        depStopField.textField().setPromptText("Nom de l'arrêt de départ");
        arrStopField.textField().setPromptText("Nom de l'arrêt d'arrivée");

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("H:mm");
        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(
                new LocalTimeStringConverter(displayFormatter, inputFormatter),
                LocalTime.now()
        );
        timeField.setTextFormatter(timeFormatter);

        Button exchange = new Button("↔");
        exchange.setId("exchange");

        exchange.setOnAction(e -> {
            String temp = depStopField.textField().getText();
            depStopField.setTo(arrStopField.textField().getText());
            arrStopField.setTo(temp);
        });

        // Création des conteneurs
        HBox text = new HBox(10, depStopLabel, depStopField.textField(), exchange, arrStopLabel, arrStopField.textField());
        HBox date = new HBox(10, dateLabel, datePicker, timeLabel, timeField);

        // Création du conteneur principal
        VBox root = new VBox(10, text, date);
        root.setId("query");
        root.getStylesheets().add("query.css");

        return new QueryUI(
                root,
                depStopField.stopO(),
                arrStopField.stopO(),
                datePicker.valueProperty(),
                timeFormatter.valueProperty()
        );
    }
}