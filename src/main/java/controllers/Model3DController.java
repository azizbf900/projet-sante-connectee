package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la visualisation des modèles 3D
 */
public class Model3DController implements Initializable {

    @FXML private Button closeBtn;
    @FXML private Button fullscreenBtn;
    @FXML private StackPane modelContainer;

    private String modelPath;
    private boolean isFullscreen = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les écouteurs d'événements
        closeBtn.setOnAction(event -> {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        });

        fullscreenBtn.setOnAction(event -> toggleFullscreen());
    }

    /**
     * Définit le chemin du modèle 3D à afficher
     */
    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
        loadModel();
    }

    /**
     * Charge le modèle 3D dans la WebView
     */
    private void loadModel() {
        // Selon le format de votre modèle 3D, vous pouvez utiliser différentes technologies
        // Cet exemple utilise une WebView pour charger un visualiseur WebGL

        String html = generateModelViewerHtml();
    }

    /**
     * Génère le code HTML pour afficher le modèle 3D
     */
    private String generateModelViewerHtml() {
        // Utilise model-viewer (Google) pour afficher différents formats 3D
        // Assurez-vous d'avoir les scripts nécessaires disponibles

        String formatted = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Visualisation 3D</title>
                  <script type="module" src="https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js"></script>
                  <style>
                    body { margin: 0; padding: 0; overflow: hidden; }
                    model-viewer { width: 100%; height: 100vh; background-color: #f5f5f5; }
                  </style>
                </head>
                <body>
                  <model-viewer 
                    src="%s" 
                    alt="Modèle 3D" 
                    auto-rotate 
                    camera-controls 
                    shadow-intensity="1" 
                    exposure="0.75"
                    ar-status="not-presenting">
                  </model-viewer>
                </body>
                </html>
                """.formatted(modelPath);
        return formatted;
    }

    /**
     * Bascule entre le mode plein écran et le mode normal
     */
    private void toggleFullscreen() {
        Stage stage = (Stage) fullscreenBtn.getScene().getWindow();
        isFullscreen = !isFullscreen;
        stage.setFullScreen(isFullscreen);
    }

    /**
     * Méthode statique utilitaire pour ouvrir un modèle 3D depuis n'importe où
     */
    public static void openModel(String modelPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(Model3DController.class.getResource("/front/model-3d.fxml"));
        Parent root = loader.load();

        Model3DController controller = loader.getController();
        controller.setModelPath(modelPath);

        Stage stage = new Stage();
        stage.setTitle("Visualisation 3D - " + title);
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }
}