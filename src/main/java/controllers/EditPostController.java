package controllers;

import Services.ServicePosts;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.Posts;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;

public class EditPostController {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea legendeField;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Button chooseImageBtn;

    @FXML
    private Button updateBtn;

    @FXML
    private Label errorLabel;

    private File selectedImage;
    private Posts currentPost;
    private final ServicePosts servicePosts = new ServicePosts();

    private final String destinationFolder = "src/main/resources/images/";

    public void setPostToEdit(Posts post) {
        this.currentPost = post;
        titreField.setText(post.getTitre());
        legendeField.setText(post.getLegende());

        if (post.getContenu() != null) {
            File imageFile = new File(destinationFolder + post.getContenu());
            if (imageFile.exists()) {
                imagePreview.setImage(new Image(imageFile.toURI().toString()));
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une nouvelle image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImage = file;
            imagePreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleUpdatePost() {
        String titre = titreField.getText().trim();
        String legende = legendeField.getText().trim();

        if (titre.isEmpty() || legende.isEmpty()) {
            errorLabel.setText("❌ Veuillez remplir tous les champs obligatoires.");
            return;
        }

        String imageName = currentPost.getContenu(); // valeur par défaut

        // Si une nouvelle image est choisie
        if (selectedImage != null) {
            try {
                imageName = selectedImage.getName();
                Path destPath = Paths.get(destinationFolder + imageName);
                Files.copy(selectedImage.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                errorLabel.setText("❌ Erreur lors de la copie de la nouvelle image.");
                e.printStackTrace();
                return;
            }
        }

        // Mise à jour de l'objet
        currentPost.setTitre(titre);
        currentPost.setLegende(legende);
        currentPost.setContenu(imageName);
        currentPost.setDatePublication(LocalDate.now());

        // Appel du service
        servicePosts.modifierPost(currentPost);
        errorLabel.setText("✅ Post modifié avec succès !");
    }
}
