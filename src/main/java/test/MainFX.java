package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/categorie.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("VitalLink Application - Categorie");
        primaryStage.setScene(new Scene(root, 1500, 780)); // Tu peux ajuster si besoin
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}