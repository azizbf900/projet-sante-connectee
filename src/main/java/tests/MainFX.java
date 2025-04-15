package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger l'interface FXML
            Parent root = FXMLLoader.load(getClass().getResource("/Views/BackPostTable.fxml")); // adapte si besoin
            primaryStage.setTitle("Ajouter un Post");
            primaryStage.setScene(new Scene(root, 800, 600)); // adapte la taille si nécessaire
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
