package controllers;

import Services.ServicePosts;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Posts;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class BackPostTableController {

    @FXML
    private TableView<Posts> tablePosts;

    @FXML
    private TableColumn<Posts, Integer> colId;

    @FXML
    private TableColumn<Posts, String> colTitre;

    @FXML
    private TableColumn<Posts, String> colContenu;

    @FXML
    private TableColumn<Posts, LocalDate> colDate;

    @FXML
    private TableColumn<Posts, String> colLegende;

    @FXML
    private TableColumn<Posts, Void> colActions;

    private final ServicePosts servicePosts = new ServicePosts();
    private ObservableList<Posts> listPosts;

    @FXML
    public void initialize() {
        afficherPosts();

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");

            {
                btnEdit.setOnAction(event -> {
                    Posts post = getTableView().getItems().get(getIndex());
                    // Redirection possible vers interface de modification si tu veux
                    showAlert(Alert.AlertType.INFORMATION, "Modifier", "Fonction de modification à implémenter.");
                });

                btnDelete.setOnAction(event -> {
                    Posts post = getTableView().getItems().get(getIndex());
                    confirmDelete(post);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10, btnEdit, btnDelete);
                    setGraphic(box);
                }
            }
        });
    }

    private void afficherPosts() {
        listPosts = FXCollections.observableArrayList(servicePosts.getAllPosts());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colLegende.setCellValueFactory(new PropertyValueFactory<>("legende"));

        tablePosts.setItems(listPosts);
    }

    private void confirmDelete(Posts post) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression du post");
        alert.setContentText("Voulez-vous vraiment supprimer ce post ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            servicePosts.supprimerPost(post.getId());
            afficherPosts();
        }
    }

    @FXML
    private void allerVersAjoutPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AjoutPost.fxml")); // Mets ici le bon chemin vers ton fichier FXML
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Post");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'interface d'ajout de post.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
