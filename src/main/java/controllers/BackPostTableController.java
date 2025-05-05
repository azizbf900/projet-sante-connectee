package controllers;

import Services.ServicePosts;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Posts;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class BackPostTableController {

    @FXML private VBox postsContainer;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final ServicePosts servicePosts = new ServicePosts();
    private int currentPage = 1;
    private final int itemsPerPage = 5;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        updatePagination();
    }

    // Méthodes de pagination
    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    @FXML
    private void nextPage() {
        List<Posts> allPosts = servicePosts.getAllPosts();
        int totalPages = (int) Math.ceil((double) allPosts.size() / itemsPerPage);

        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    private void updatePagination() {
        List<Posts> allPosts = servicePosts.getAllPosts();
        int totalPages = Math.max(1, (int) Math.ceil((double) allPosts.size() / itemsPerPage));

        // Ajuster currentPage si invalide (après suppression)
        currentPage = Math.min(currentPage, totalPages);

        pageInfo.setText("Page " + currentPage + "/" + totalPages);
        prevBtn.setDisable(currentPage == 1);
        nextBtn.setDisable(currentPage == totalPages || totalPages == 0);

        refreshPosts();
    }

    private void refreshPosts() {
        postsContainer.getChildren().clear();
        List<Posts> visiblePosts = getPaginatedPosts();

        if (visiblePosts.isEmpty()) {
            Label emptyLabel = new Label("Aucun post à afficher");
            emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
            postsContainer.getChildren().add(emptyLabel);
            return;
        }

        visiblePosts.forEach(post -> {
            VBox postCard = createPostCard(post);
            postsContainer.getChildren().add(postCard);
        });
    }

    private List<Posts> getPaginatedPosts() {
        List<Posts> allPosts = servicePosts.getAllPosts();
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allPosts.size());

        return allPosts.subList(start, end);
    }

    private VBox createPostCard(Posts post) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        card.setMaxWidth(800);

        // Header (ID + Date)
        HBox header = new HBox(10);
        Label idLabel = new Label("#" + post.getId());
        Label dateLabel = new Label(post.getDatePublication().format(DATE_FORMAT));
        header.getChildren().addAll(idLabel, dateLabel);
        idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6c757d;");
        dateLabel.setStyle("-fx-text-fill: #6c757d;");

        // Titre
        Label titleLabel = new Label(post.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #343a40;");
        titleLabel.setWrapText(true);

        // Légende
        Label legendLabel = new Label(post.getLegende());
        legendLabel.setStyle("-fx-text-fill: #495057;");
        legendLabel.setWrapText(true);

        // Image
        ImageView imageView = new ImageView();
        if (post.getContenu() != null && !post.getContenu().isEmpty()) {
            try {
                Image img = new Image("file:src/main/resources/images/" + post.getContenu());
                imageView.setImage(img);
                imageView.setFitWidth(200);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                System.err.println("Image non trouvée: " + post.getContenu());
            }
        }

        // Boutons
        HBox buttonsBox = new HBox(10);
        Button editBtn = new Button("Modifier");
        Button deleteBtn = new Button("Supprimer");

        editBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");

        editBtn.setOnAction(e -> openEditDialog(post));
        deleteBtn.setOnAction(e -> confirmDelete(post));

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(header, titleLabel, legendLabel, imageView, buttonsBox);
        return card;
    }

    private void openEditDialog(Posts post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EditPost.fxml"));
            Parent root = loader.load();

            EditPostController controller = loader.getController();
            controller.setPostToEdit(post);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Post");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            updatePagination(); // Recharge avec potentiellement nouvelle page
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'éditeur");
        }
    }

    private void confirmDelete(Posts post) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression du post #" + post.getId());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce post?\nTitre: " + post.getTitre());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            servicePosts.supprimerPost(post.getId());
            updatePagination(); // Met à jour l'affichage et la pagination
        }
    }

    @FXML
    private void allerVersAjoutPost() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AjoutPost.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Post");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface d'ajout");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}