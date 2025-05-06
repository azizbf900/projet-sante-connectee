package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class MainLayoutController implements Initializable {

    @FXML
    private StackPane contentArea;

    @FXML
    private HBox btnDashboard;
    @FXML
    private HBox btnUsers;
    @FXML
    private HBox btnRendezVous;
    @FXML
    private HBox btnProduits;
    @FXML
    private HBox btnEvents;
    @FXML
    private HBox btnBlogs;
    @FXML
    private HBox btnSettings;
    @FXML
    private HBox btnLogout;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Chargement initial du dashboard
        loadView("/ListProduit.fxml");
        setActiveButton(btnDashboard);
    }

    public void handleDashboardClick(MouseEvent mouseEvent) {
        setActiveButton(btnDashboard);
        loadView("/ListProduit.fxml");
    }

    public void handleUsersClick(MouseEvent mouseEvent) {
        setActiveButton(btnUsers);
        loadView("/users.fxml");
    }

    public void handleRendezVousClick(MouseEvent mouseEvent) {
        setActiveButton(btnRendezVous);
        loadView("/rendez_vous.fxml");
    }

    public void handleProduitsClick(MouseEvent mouseEvent) {
        setActiveButton(btnProduits);
        loadView("/ListProduit.fxml");
    }

    public void handleEventsClick(MouseEvent mouseEvent) {
        setActiveButton(btnEvents);
        loadView("/events.fxml");
    }

    public void handleBlogsClick(MouseEvent mouseEvent) {
        setActiveButton(btnBlogs);
        loadView("/views/blogs.fxml");
    }

    public void handleSettingsClick(MouseEvent mouseEvent) {
        setActiveButton(btnSettings);
        loadView("/views/settings.fxml");
    }

    public void handleLogoutClick(MouseEvent mouseEvent) {
        System.out.println("Déconnexion...");
        // Ici, vous pouvez rediriger vers la scène de connexion
        // Exemple : Platform.exit(); ou changement de scène
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue : " + fxmlPath);
        }
    }

    private void setActiveButton(HBox activeBtn) {
        // Supprimer "active" de tous
        if (btnDashboard != null) btnDashboard.getStyleClass().remove("active");
        if (btnUsers != null) btnUsers.getStyleClass().remove("active");
        if (btnRendezVous != null) btnRendezVous.getStyleClass().remove("active");
        if (btnProduits != null) btnProduits.getStyleClass().remove("active");
        if (btnEvents != null) btnEvents.getStyleClass().remove("active");
        if (btnBlogs != null) btnBlogs.getStyleClass().remove("active");
        if (btnSettings != null) btnSettings.getStyleClass().remove("active");

        // Ajouter "active" au bouton cliqué
        if (activeBtn != null && !activeBtn.getStyleClass().contains("active")) {
            activeBtn.getStyleClass().add("active");
        }
    }
}
