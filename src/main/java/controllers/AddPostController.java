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

    private final String destinationFolder = "resources/images/"; // ou "resources/images/"

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
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
            // 1. Créer le dossier s'il n'existe pas
            File directory = new File(destinationFolder);
            if (!directory.exists()) {
                directory.mkdirs(); // crée le dossier et ses parents si besoin
            }

            // 2. Chemin absolu vers le fichier de destination
            Path destPath = Paths.get(destinationFolder, selectedImage.getName()).toAbsolutePath();

            // 3. Copier l’image
            Files.copy(selectedImage.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

            // 4. Créer l’objet Post
            Posts post = new Posts(titre, selectedImage.getName(), LocalDate.now(), legende);
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
