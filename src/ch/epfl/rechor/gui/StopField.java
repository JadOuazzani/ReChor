package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;

import java.util.List;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

public record StopField(TextField textField, ObservableValue<String> stopO) {
    public static StopField create(StopIndex stopIndex) {
        TextField textField = new TextField();
        StringProperty stopProperty = new SimpleStringProperty("");

        Popup window = new Popup();
        window.setHideOnEscape(false);

        ListView<String> listView = new ListView<>();
        listView.setFocusTraversable(false);
        listView.setMaxHeight(240);

        window.getContent().add(listView);

        ChangeListener<String> textListener = (observable1, oldValue1, newValue1) -> {
            List<String> matches = stopIndex.stopsMatching(newValue1, 10);
            listView.getItems().setAll(matches);
            if (!matches.isEmpty()) {
                listView.getSelectionModel().select(0);
            }
        };

        ChangeListener<Bounds> boundsListener = (observable2, oldBound, newBound) -> {
            Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
            window.setAnchorX(bounds.getMinX());
            window.setAnchorY(bounds.getMaxY());
        };

        textField.addEventHandler(KEY_PRESSED, event -> {
            if (UP == event.getCode()) {
                int selected = listView.getSelectionModel().getSelectedIndex();
                if (selected > 0) {
                    listView.getSelectionModel().select(selected - 1);
                    event.consume();
                }
            } else if (DOWN == event.getCode()) {
                int selected = listView.getSelectionModel().getSelectedIndex();
                if (selected < listView.getItems().size() - 1) {
                    listView.getSelectionModel().select(selected + 1);
                    event.consume();
                }
            }
        });

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                window.show(textField.getScene().getWindow());
                textField.textProperty().addListener(textListener);
                textField.boundsInLocalProperty().addListener(boundsListener);
            } else {
                textField.textProperty().removeListener(textListener);
                textField.boundsInLocalProperty().removeListener(boundsListener);
                window.hide();

                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    String selectedStop = listView.getItems().get(selectedIndex);
                    textField.setText(selectedStop);
                    stopProperty.setValue(selectedStop);
                } else {
                    textField.setText("");
                    stopProperty.setValue("");
                }
            }
        });

        return new StopField(textField, stopProperty);
    }

    public void setTo(String stopName) {
        textField.setText(stopName);
    }
}