package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Commentaire;
import models.Posts;
import Services.ServiceCommentaire;
import Services.ServicePosts;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import Services.TranslateService;

public class PostCardController {

    @FXML
    private ImageView postImage;

    @FXML
    private Label postTitle;

    @FXML
    private Label postContent;

    @FXML
    private Label postDate;

    @FXML
    private VBox commentContainer;

    @FXML
    private TextField commentInput;

    @FXML
    private AnchorPane rootCard;

    @FXML
    private Label likeCountLabel;

    @FXML
    private Label dislikeCountLabel;

    private Posts post;

    private final ServiceCommentaire serviceCommentaire = new ServiceCommentaire();
    private final ServicePosts servicePosts = new ServicePosts();

    public void initialize() {
        rootCard.setOnMouseClicked(event -> {
            if (post != null) {
                openDetailPage();
            }
        });
    }

    public void setPostData(Posts post) {
        this.post = post;

        postTitle.setText(post.getTitre());
        postContent.setText(post.getLegende());
        postDate.setText(post.getDatePublication().toString());

        String imagePath = "/images/" + post.getContenu();
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream != null) {
            postImage.setImage(new Image(imageStream));
        } else {
            System.err.println("❌ Image non trouvée : " + imagePath);
        }

        updateLikeDislikeLabels();
        loadComments();
    }

    private void updateLikeDislikeLabels() {
        if (likeCountLabel != null && dislikeCountLabel != null && post != null) {
            likeCountLabel.setText(String.valueOf(post.getLike()));
            dislikeCountLabel.setText(String.valueOf(post.getDislike()));
        }
    }

    private void loadComments() {
        commentContainer.getChildren().removeIf(node -> node instanceof HBox);

        if (post == null) return;

        List<Commentaire> commentaires = serviceCommentaire.getCommentairesByPostId(post.getId());

        for (Commentaire c : commentaires) {
            HBox commentBox = new HBox(10);
            Label content = new Label(c.getContenu());

            Button btnEdit = new Button("✏️");
            Button btnDelete = new Button("❌");

            btnDelete.setOnAction(e -> {
                serviceCommentaire.supprimer(c.getId());
                loadComments();
            });

            btnEdit.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog(c.getContenu());
                dialog.setTitle("Modifier commentaire");
                dialog.setHeaderText(null);
                dialog.setContentText("Modifier le texte :");

                dialog.showAndWait().ifPresent(newText -> {
                    c.setContenu(newText);
                    c.setDateCommentaire(LocalDate.now());
                    serviceCommentaire.modifier(c);
                    loadComments();
                });
            });

            commentBox.getChildren().addAll(content, btnEdit, btnDelete);
            commentContainer.getChildren().add(commentBox);
        }
    }

    @FXML
    private void handleAddComment() {
        if (post == null) {
            System.err.println("❌ Erreur : post non initialisé !");
            return;
        }

        String text = commentInput.getText().trim();
        if (!text.isEmpty()) {
            Commentaire nouveau = new Commentaire(post.getId(), text, LocalDate.now());
            serviceCommentaire.ajouter(nouveau);
            commentInput.clear();
            loadComments();
        }
    }

    @FXML
    private void handleLike() {
        if (post != null) {
            post.setLike(post.getLike() + 1);
            servicePosts.updateLikesDislikes(post);
            updateLikeDislikeLabels();
        }
    }

    @FXML
    private void handleDislike() {
        if (post != null) {
            post.setDislike(post.getDislike() + 1);
            servicePosts.updateLikesDislikes(post);
            updateLikeDislikeLabels();
        }
    }


    @FXML
    private void handleTranslate() {
        if (post != null) {
            String texteOriginal = postContent.getText();
            String texteTraduit = TranslateService.translateText(texteOriginal, "fr", "en");
            postContent.setText(texteTraduit);
        }
    }

    private void openDetailPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/DetailPost.fxml"));
            Parent root = loader.load();

            DetailPostController controller = loader.getController();
            controller.setPostData(
                    "/images/" + post.getContenu(),
                    post.getTitre(),
                    post.getDatePublication().toString(),
                    post.getLegende(),
                    serviceCommentaire.getCommentairesByPostId(post.getId()).size()
            );

            Stage stage = (Stage) rootCard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détail du Post");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
