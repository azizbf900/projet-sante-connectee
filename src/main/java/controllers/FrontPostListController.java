package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.Posts;
import Services.ServicePosts;

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

        // Recherche en temps réel
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Tri
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        applyFilters(); // Initial
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
                        return Integer.compare(pop2, pop1); // décroissant
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        renderPosts(filtered);
    }

    private int getPopularite(Posts post) {
        int nbCommentaires = post.getCommentaires() != null ? post.getCommentaires().size() : 0;
        int nbLikes = post.getLike(); // suppose que post.getLike() retourne un int, sinon protège-le aussi
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
}
