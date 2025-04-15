package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import models.Posts;
import Services.ServicePosts;

import java.io.IOException;
import java.util.List;

public class FrontPostListController {

    @FXML
    private VBox postContainer;

    private final ServicePosts servicePosts = new ServicePosts();

    @FXML
    public void initialize() {
        loadPosts();
    }

    private void loadPosts() {
        List<Posts> posts = servicePosts.getAllPosts(); // Assure-toi que cette méthode existe bien

        for (Posts post : posts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/PostCard.fxml"));
                Node card = loader.load();

                // Récupérer le contrôleur de la carte
                PostCardController controller = loader.getController();
                controller.setPostData(post); // Passer l'objet Post complet

                // Ajouter la carte au conteneur
                postContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erreur lors du chargement de PostCard.fxml : " + e.getMessage());
            }
        }
    }
}
