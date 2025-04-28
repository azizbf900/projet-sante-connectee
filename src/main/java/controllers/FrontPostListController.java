package controllers;

import Services.ServicePosts;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Posts;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class FrontPostListController {

    @FXML
    private VBox postContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private AnchorPane menuPane; // Menu latéral masqué/visible

    @FXML
    private VBox contentBox; // Conteneur du contenu principal

    @FXML
    private HBox rootBox; // Le HBox parent qui contient menu + contenu

    private boolean menuVisible = false; // Suivi état menu

    private final ServicePosts servicePosts = new ServicePosts();
    private List<Posts> allPosts;

    @FXML
    public void initialize() {
        allPosts = servicePosts.getAllPosts();

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Plus récents d'abord",
                "Plus anciens d'abord",
                "Plus populaires"
        ));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        applyFilters();
    }

    private void applyFilters() {
        String searchTerm = searchField.getText().toLowerCase(Locale.ROOT);
        String sortOption = sortComboBox.getValue();

        List<Posts> filtered = allPosts.stream()
                .filter(post -> post.getTitre().toLowerCase(Locale.ROOT).contains(searchTerm))
                .sorted((p1, p2) -> {
                    if ("Plus récents d'abord".equals(sortOption)) {
                        return p2.getDatePublication().compareTo(p1.getDatePublication());
                    } else if ("Plus anciens d'abord".equals(sortOption)) {
                        return p1.getDatePublication().compareTo(p2.getDatePublication());
                    } else if ("Plus populaires".equals(sortOption)) {
                        int pop1 = getPopularite(p1);
                        int pop2 = getPopularite(p2);
                        return Integer.compare(pop2, pop1);
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        renderPosts(filtered);
    }

    private int getPopularite(Posts post) {
        int nbCommentaires = post.getCommentaires() != null ? post.getCommentaires().size() : 0;
        int nbLikes = post.getLike();
        return nbCommentaires + nbLikes;
    }

    private void renderPosts(List<Posts> postsToDisplay) {
        postContainer.getChildren().clear();

        for (Posts post : postsToDisplay) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/PostCard.fxml"));
                Node card = loader.load();

                PostCardController controller = loader.getController();
                controller.setPostData(post);

                postContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleManagePosts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/BackPostTable.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Posts - Backend");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🍔 Bouton burger toggle menu
    @FXML
    private void toggleMenu() {
        if (menuVisible) {
            // Disparition
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), menuPane);
            slideOut.setToX(-menuPane.getWidth());

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), menuPane);
            fadeOut.setToValue(0);

            ParallelTransition transition = new ParallelTransition(slideOut, fadeOut);
            transition.setOnFinished(e -> menuPane.setVisible(false));
            transition.play();

            menuVisible = false;
        } else {
            menuPane.setVisible(true);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), menuPane);
            slideIn.setFromX(-menuPane.getWidth());
            slideIn.setToX(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), menuPane);
            fadeIn.setToValue(1);

            new ParallelTransition(slideIn, fadeIn).play();

            menuVisible = true;
        }
    }
}
