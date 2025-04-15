package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.Posts;
import Services.ServicePosts;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;

public class AddPostController {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea legendeField;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Label errorLabel;

    private File selectedImage;
    private final ServicePosts servicePosts = new ServicePosts();

    // Dossier de destination relatif au projet
    private final String destinationFolder = "uploads/images/";

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
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
    private void handleAddPost() {
        String titre = titreField.getText().trim();
        String legende = legendeField.getText().trim();

        if (titre.isEmpty() || legende.isEmpty() || selectedImage == null) {
            errorLabel.setText("❌ Veuillez remplir tous les champs et choisir une image.");
            return;
        }

        try {
            String sanitizedImageName = selectedImage.getName().replaceAll("[^a-zA-Z0-9.\\-_]", "_");
            Path destPath = Paths.get(destinationFolder + sanitizedImageName);

            Files.createDirectories(destPath.getParent()); // Création auto du dossier si nécessaire
            Files.copy(selectedImage.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

            Posts post = new Posts(titre, sanitizedImageName, LocalDate.now(), legende);
            servicePosts.ajouterPost(post);

            errorLabel.setText("✅ Post ajouté avec succès !");
            clearFields();
        } catch (IOException e) {
            errorLabel.setText("❌ Erreur lors de la copie de l'image : " + e.getMessage());
            e.printStackTrace();
        }

    }

    @FXML
    private void handleCancel() {
        clearFields();
    }

    private void clearFields() {
        titreField.clear();
        legendeField.clear();
        imagePreview.setImage(null);
        selectedImage = null;
        errorLabel.setText("");
    }
}
