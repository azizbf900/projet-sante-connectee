package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le layout principal
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/main_layout.fxml")));

        // Configurer la scène
        Scene scene = new Scene(root, 1200, 800);

        // Configurer la fenêtre
        primaryStage.setTitle("SanaConnect - Système de Gestion");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}