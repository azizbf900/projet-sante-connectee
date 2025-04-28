package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Category;
import models.Produit;
import services.CategorieService;
import services.ProduitService;
import utils.NotificationUtil;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AjoutProduit implements Initializable {

    @FXML
    private TextField nomField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField prixField;
    @FXML
    private TextField quantiteField;
    @FXML
    private ComboBox<Category> categorieComboBox;
    @FXML
    private TextField imagePathField;
    @FXML
    private TextField model3DField;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Button submitButton;

    private final ProduitService produitService = ProduitService.getInstance();
    private final CategorieService categorieService = new CategorieService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger les catégories dans le ComboBox
        chargerCategories();

        // Validation en temps réel
        ajouterValidation();
    }

    private void chargerCategories() {
        try {
            // Récupérer les catégories depuis le service
            categorieComboBox.setItems(FXCollections.observableArrayList(categorieService.getAll()));

            // Personnaliser l'affichage des catégories
            categorieComboBox.setCellFactory(cb -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getName());
                }
            });

            categorieComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getName());
                }
            });
        } catch (Exception e) {
            NotificationUtil.showError("Erreur lors du chargement des catégories : " + e.getMessage());
        }
    }

    private void ajouterValidation() {
        // Validation pour le champ prix (uniquement des nombres)
        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                prixField.setText(oldValue);
            }
        });

        // Validation pour le champ quantité (uniquement des nombres entiers)
        quantiteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantiteField.setText(oldValue);
            }
        });

        // Activer/désactiver le bouton submit selon si les champs requis sont remplis
        submitButton.disableProperty().bind(
                nomField.textProperty().isEmpty()
                        .or(descriptionField.textProperty().isEmpty())
                        .or(prixField.textProperty().isEmpty())
                        .or(quantiteField.textProperty().isEmpty())
                        .or(categorieComboBox.valueProperty().isNull())
        );
    }

    @FXML
    public void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Filtres pour images
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(nomField.getScene().getWindow());
        if (selectedFile != null) {
            imagePathField.setText(selectedFile.getAbsolutePath());

            // Afficher un aperçu de l'image
            try {
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
            } catch (Exception e) {
                NotificationUtil.showError("Impossible de charger l'aperçu de l'image");
            }
        }
    }

    @FXML
    public void choisirModel3D() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un modèle 3D");

        // Filtres pour modèles 3D
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Modèles 3D", "*.obj", "*.fbx", "*.gltf", "*.glb", "*.dae"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(nomField.getScene().getWindow());
        if (selectedFile != null) {
            model3DField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    public void ajouterProduit() {
        try {
            // Validation des champs avant soumission
            if (!validerChamps()) {
                return;
            }

            double prix = Double.parseDouble(prixField.getText().trim());
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            int categorieId = categorieComboBox.getValue().getId();

            Produit produit = new Produit(
                    nomField.getText().trim(),
                    descriptionField.getText().trim(),
                    prix,
                    quantite,
                    categorieId,
                    imagePathField.getText().trim(),
                    model3DField.getText().trim()
            );

            produitService.ajouterProduit(produit);
            NotificationUtil.showInfo("Produit ajouté avec succès !");

            // Réinitialiser le formulaire après succès
            reinitialiser();
        } catch (NumberFormatException e) {
            NotificationUtil.showError("Veuillez entrer des valeurs numériques valides pour le prix et la quantité.");
        } catch (Exception e) {
            NotificationUtil.showError("Erreur lors de l'ajout du produit : " + e.getMessage());
        }
    }

    private boolean validerChamps() {
        if (nomField.getText().trim().isEmpty()) {
            NotificationUtil.showError("Le nom du produit est requis");
            nomField.requestFocus();
            return false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            NotificationUtil.showError("La description du produit est requise");
            descriptionField.requestFocus();
            return false;
        }

        if (prixField.getText().trim().isEmpty()) {
            NotificationUtil.showError("Le prix du produit est requis");
            prixField.requestFocus();
            return false;
        }

        if (quantiteField.getText().trim().isEmpty()) {
            NotificationUtil.showError("La quantité est requise");
            quantiteField.requestFocus();
            return false;
        }

        if (categorieComboBox.getValue() == null) {
            NotificationUtil.showError("Veuillez sélectionner une catégorie");
            categorieComboBox.requestFocus();
            return false;
        }

        // Vérifier que le prix est positif
        try {
            double prix = Double.parseDouble(prixField.getText().trim());
            if (prix < 0) {
                NotificationUtil.showError("Le prix doit être positif");
                prixField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationUtil.showError("Prix invalide");
            prixField.requestFocus();
            return false;
        }

        // Vérifier que la quantité est positive ou nulle
        try {
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            if (quantite < 0) {
                NotificationUtil.showError("La quantité ne peut pas être négative");
                quantiteField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationUtil.showError("Quantité invalide");
            quantiteField.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    public void annuler() {
        // Fermer la fenêtre
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void reinitialiser() {
        nomField.clear();
        descriptionField.clear();
        prixField.clear();
        quantiteField.clear();
        categorieComboBox.getSelectionModel().clearSelection();
        imagePathField.clear();
        model3DField.clear();
        imagePreview.setImage(null);
    }
}