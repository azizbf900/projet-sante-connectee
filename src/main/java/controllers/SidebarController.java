package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

public class SidebarController implements Initializable {

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
        // Initialisation du contrôleur de la sidebar
        // Ce contrôleur est principalement utilisé pour exposer les éléments de la sidebar
        // au contrôleur principal (MainLayoutController)
    }

    // Les getters permettent au MainLayoutController d'accéder aux boutons
    public HBox getBtnDashboard() {
        return btnDashboard;
    }

    public HBox getBtnUsers() {
        return btnUsers;
    }

    public HBox getBtnRendezVous() {
        return btnRendezVous;
    }

    public HBox getBtnProduits() {
        return btnProduits;
    }

    public HBox getBtnEvents() {
        return btnEvents;
    }

    public HBox getBtnBlogs() {
        return btnBlogs;
    }

    public HBox getBtnSettings() {
        return btnSettings;
    }

    public HBox getBtnLogout() {
        return btnLogout;
    }
}