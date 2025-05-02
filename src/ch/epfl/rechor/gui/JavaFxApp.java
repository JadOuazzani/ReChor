package ch.epfl.rechor.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public final class JavaFxApp extends Application {
    // Constructeur public (important pour JavaFX)
    public JavaFxApp() {}

    public static void main(String[] args) {
        launch(args); // Lance l'application JavaFX
    }

    @Override
    public void start(Stage primaryStage) {
        Button button = new Button("Click me!");
        button.setOnAction(e -> System.out.println("Bouton cliqué !"));

        Scene scene = new Scene(button, 300, 200);
        primaryStage.setTitle("Ma Première App JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}