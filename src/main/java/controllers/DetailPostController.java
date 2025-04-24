package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.InputStream;

public class DetailPostController {

    @FXML
    private ImageView imageView; // Image du post
    @FXML
    private Label titreLabel; // Titre du post
    @FXML
    private Label dateLabel; // Date du post
    @FXML
    private Label legendeLabel; // Légende du post
    @FXML
    private Label commentCountLabel; // Nombre de commentaires
    @FXML
    private VBox commentsSection; // Section des commentaires
    @FXML
    private Button backButton; // Bouton pour revenir à la liste

    private String imageName;
    private String postTitle;
    private String postDate;
    private String postLegende;
    private int commentCount;

    public void setPostData(String imageName, String titre, String date, String legende, int nbCommentaires) {
        this.imageName = imageName;
        this.postTitle = titre;
        this.postDate = date;
        this.postLegende = legende;
        this.commentCount = nbCommentaires;

        // Charger l'image depuis le dossier resources/images
        InputStream stream = getClass().getResourceAsStream(imageName.startsWith("/images/") ? imageName : "/images/" + imageName);

        if (stream != null) {
            imageView.setImage(new Image(stream));
        } else {
            System.err.println("❌ Image introuvable dans les ressources : /images/" + imageName);
        }

        titreLabel.setText(postTitle);
        dateLabel.setText("Publié le : " + postDate);
        legendeLabel.setText(postLegende);
        commentCountLabel.setText("Commentaires : " + commentCount);

        loadComments();
    }

    private void loadComments() {
        commentsSection.getChildren().clear();

        // Exemple : afficher des commentaires fictifs
        for (int i = 0; i < commentCount; i++) {
            Label commentLabel = new Label("Commentaire " + (i + 1) + " : Ceci est un commentaire exemple.");
            commentsSection.getChildren().add(commentLabel);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/FrontPostList.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors du retour à la liste des posts.");
        }
    }


}
