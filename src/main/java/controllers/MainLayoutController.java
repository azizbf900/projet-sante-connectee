package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class MainLayoutController implements Initializable {

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox sidebar;

    // Les références aux boutons de la sidebar
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
        // Vérification que les éléments de la sidebar sont correctement injectés
        if (btnDashboard == null) {
            System.err.println("btnDashboard est null. Vérifiez l'injection FXML.");
        }

        // Chargement du dashboard par défaut
        loadView("/ListProduit.fxml");

        // Configuration des gestionnaires d'événements pour les boutons de navigation
        setupSidebarNavigation();
    }

    private void setupSidebarNavigation() {
        // Ajouter des vérifications null pour tous les boutons

        // Dashboard
        if (btnDashboard != null) {
            btnDashboard.setOnMouseClicked(event -> {
                setActiveButton(btnDashboard);
                loadView("/ListProduit.fxml");
            });
        }

        // Utilisateurs
        if (btnUsers != null) {
            btnUsers.setOnMouseClicked(event -> {
                setActiveButton(btnUsers);
                loadView("/users.fxml");
            });
        }

        // Rendez-vous
        if (btnRendezVous != null) {
            btnRendezVous.setOnMouseClicked(event -> {
                setActiveButton(btnRendezVous);
                loadView("/rendez_vous.fxml");
            });
        }

        // Produits
        if (btnProduits != null) {
            btnProduits.setOnMouseClicked(event -> {
                setActiveButton(btnProduits);
                loadView("/ListProduit.fxml");
            });
        }

        // Événements
        if (btnEvents != null) {
            btnEvents.setOnMouseClicked(event -> {
                setActiveButton(btnEvents);
                loadView("/events.fxml");
            });
        }

        // Blogs
        if (btnBlogs != null) {
            btnBlogs.setOnMouseClicked(event -> {
                setActiveButton(btnBlogs);
                loadView("/views/blogs.fxml");
            });
        }

        // Paramètres
        if (btnSettings != null) {
            btnSettings.setOnMouseClicked(event -> {
                setActiveButton(btnSettings);
                loadView("/views/settings.fxml");
            });
        }

        // Déconnexion
        if (btnLogout != null) {
            btnLogout.setOnMouseClicked(event -> {
                // Traitement de la déconnexion
                handleLogout();
            });
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Effacer le contenu actuel et charger la nouvelle vue
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue : " + fxmlPath);
        }
    }

    private void setActiveButton(HBox button) {
        // Vérification des null avant de manipuler les styles
        if (btnDashboard != null) btnDashboard.getStyleClass().remove("active");
        if (btnUsers != null) btnUsers.getStyleClass().remove("active");
        if (btnRendezVous != null) btnRendezVous.getStyleClass().remove("active");
        if (btnProduits != null) btnProduits.getStyleClass().remove("active");
        if (btnEvents != null) btnEvents.getStyleClass().remove("active");
        if (btnBlogs != null) btnBlogs.getStyleClass().remove("active");
        if (btnSettings != null) btnSettings.getStyleClass().remove("active");

        // Définir le bouton actif
        if (button != null) {
            button.getStyleClass().add("active");
        }
    }

    private void handleLogout() {
        // Implémentez ici votre logique de déconnexion
        System.out.println("Déconnexion...");
        // Par exemple :
        // 1. Fermer la session utilisateur
        // 2. Rediriger vers l'écran de connexion
    }
}