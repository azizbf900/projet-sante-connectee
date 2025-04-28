package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import models.Category;
import models.Produit;
import services.CategorieService;
import services.ProduitService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrontProduit {

    @FXML
    private FlowPane produitsContainer;

    @FXML
    private ComboBox<Category> categorieComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Label lblHeaderTitle;

    @FXML
    private Label lblResultInfo;

    @FXML
    private Button btnRetourCategories;

    @FXML
    private Button btnAccueil;

    @FXML
    private Button voiceSearchButton;

    @FXML
    private Button searchButton;

    @FXML
    private StackPane voiceRecognitionOverlay;

    @FXML
    private Label voicePromptLabel;

    @FXML
    private TableView<Produit> produitsTable; // Assuming you have this

    private ProduitService produitService;
    private ExecutorService executorService;
    private boolean isRecording = false;
    private TargetDataLine microphoneLine;
    private CategorieService categorieService;
    private List<Produit> allProduits;
    private Integer selectedCategoryId = null;

    private final String[] colorPalette = {
            "#3498db", "#2ecc71", "#9b59b6", "#e74c3c", "#f39c12",
            "#1abc9c", "#d35400", "#16a085", "#8e44ad", "#c0392b"
    };
    private final Random random = new Random();

    @FXML
    public void initialize() {
        produitService = ProduitService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        categorieService = new CategorieService();

        // Charger toutes les catégories
        loadCategories();

        // Charger tous les produits par défaut
        loadAllProduits();

        // Configurer les actions du ComboBox
        categorieComboBox.setOnAction(event -> {
            Category selectedCategory = categorieComboBox.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                loadProductsByCategory(selectedCategory.getId());
            } else {
                loadAllProduits();
            }
        });
        // Add listener to search field for real-time filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();  // <-- utiliser ta fonction de recherche déjà écrite
        });
    }

    private void loadCategories() {
        List<Category> categories = categorieService.getAll();
        categorieComboBox.getItems().clear();
        categorieComboBox.getItems().addAll(categories);

        // Ajouter une option "Toutes les catégories"
        categorieComboBox.getItems().add(0, new Category(0, "Toutes les catégories", ""));
    }

    private void loadAllProduits() {
        allProduits = produitService.getAll();
        displayProduits(allProduits);
        lblResultInfo.setText("Affichage de tous les produits (" + allProduits.size() + ")");
        lblHeaderTitle.setText("Produits Médicaux");
        selectedCategoryId = null;
    }

    public void loadProductsByCategory(int categoryId) {
        selectedCategoryId = categoryId;

        // Sélectionner la catégorie dans le ComboBox
        for (Category cat : categorieComboBox.getItems()) {
            if (cat.getId() == categoryId) {
                categorieComboBox.getSelectionModel().select(cat);
                break;
            }
        }

        // Filtrer les produits par catégorie
        List<Produit> filteredProduits = produitService.getAll().stream()
                .filter(p -> p.getCategorie() == categoryId)
                .collect(Collectors.toList());

        // Afficher les produits filtrés
        displayProduits(filteredProduits);

        // Mettre à jour les informations d'affichage
        Category category = categorieService.getById(categoryId);
        if (category != null) {
            lblHeaderTitle.setText("Produits - " + category.getName());
            lblResultInfo.setText("Produits dans la catégorie " + category.getName() + " (" + filteredProduits.size() + ")");
        }
    }

    private void displayProduits(List<Produit> produits) {
        produitsContainer.getChildren().clear();

        if (produits.isEmpty()) {
            Label noProductsLabel = new Label("Aucun produit disponible dans cette catégorie.");
            noProductsLabel.setFont(Font.font("System", 14));
            noProductsLabel.setTextFill(Color.web("#555555"));
            produitsContainer.getChildren().add(noProductsLabel);
            return;
        }

        for (Produit produit : produits) {
            VBox produitCard = createProduitCard(produit);
            produitsContainer.getChildren().add(produitCard);
        }
    }

    private VBox createProduitCard(Produit produit) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(220);
        card.setPrefHeight(300);

        // Créer un rectangle coloré avec les premières lettres du produit
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefHeight(120);
        iconContainer.setPrefWidth(180);

        // Rectangle coloré
        String randomColor = colorPalette[Math.abs(produit.getNom().hashCode()) % colorPalette.length];
        Rectangle rectangle = new Rectangle(180, 120);
        rectangle.setFill(Color.web(randomColor));
        rectangle.setArcWidth(10);
        rectangle.setArcHeight(10);

        // Premières lettres du produit
        String initialLetters = getInitials(produit.getNom());
        Label initialsLabel = new Label(initialLetters);
        initialsLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setTextAlignment(TextAlignment.CENTER);

        iconContainer.getChildren().addAll(rectangle, initialsLabel);

        // Nom du produit
        Label nameLabel = new Label(produit.getNom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#2c3e50"));
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setMaxWidth(180);

        // Prix du produit
        Label priceLabel = new Label(String.format("%.2f €", produit.getPrix()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        priceLabel.setTextFill(Color.web("#e74c3c"));

        // Afficher la disponibilité
        HBox availabilityBox = new HBox();
        availabilityBox.setAlignment(Pos.CENTER);
        availabilityBox.setSpacing(5);

        Circle statusCircle = new Circle(5);
        statusCircle.setFill(produit.getQuantite() > 0 ? Color.web("#2ecc71") : Color.web("#e74c3c"));

        Label statusLabel = new Label(produit.getQuantite() > 0 ? "En stock" : "Rupture de stock");
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setTextFill(produit.getQuantite() > 0 ? Color.web("#2ecc71") : Color.web("#e74c3c"));

        availabilityBox.getChildren().addAll(statusCircle, statusLabel);

        // Bouton pour voir les détails du produit
        Button viewDetailsBtn = new Button("Voir détails");
        viewDetailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        viewDetailsBtn.setPrefWidth(150);
        viewDetailsBtn.setOnAction(event -> viewProductDetails(produit.getId()));

        // Ajouter les éléments à la carte
        card.getChildren().addAll(iconContainer, nameLabel, priceLabel, availabilityBox, viewDetailsBtn);

        return card;
    }

    private static class Circle extends javafx.scene.shape.Circle {
        public Circle(double radius) {
            super(radius);
        }
    }

    private String getInitials(String name) {
        StringBuilder initials = new StringBuilder();
        String[] words = name.split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
                if (initials.length() >= 2) break;  // Maximum 2 caractères
            }
        }

        // Si on n'a qu'une seule lettre, on ajoute la deuxième lettre du premier mot
        if (initials.length() == 1 && words[0].length() > 1) {
            initials.append(Character.toUpperCase(words[0].charAt(1)));
        }

        return initials.toString();
    }

    private void viewProductDetails(int produitId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/details.fxml"));
            Parent root = loader.load();

            // Transmettre l'ID du produit au contrôleur de détails
            FrontDetails controller = loader.getController();
            controller.setProduitId(produitId); // Appel direct sans réflexion

            Stage stage = (Stage) produitsContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors de la navigation vers les détails du produit: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().toLowerCase().trim();

        if (searchTerm.isEmpty()) {
            // Si la recherche est vide, on affiche tous les produits ou ceux de la catégorie sélectionnée
            if (selectedCategoryId != null) {
                loadProductsByCategory(selectedCategoryId);
            } else {
                loadAllProduits();
            }
            return;
        }

        List<Produit> searchResults;

        if (selectedCategoryId != null) {
            // Recherche dans la catégorie sélectionnée
            searchResults = allProduits.stream()
                    .filter(p -> p.getCategorie() == selectedCategoryId)
                    .filter(p -> p.getNom().toLowerCase().contains(searchTerm) ||
                            p.getDescription().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());

            Category category = categorieService.getById(selectedCategoryId);
            if (category != null) {
                lblResultInfo.setText("Résultats de recherche pour \"" + searchTerm +
                        "\" dans la catégorie " + category.getName() + " (" + searchResults.size() + ")");
            }
        } else {
            // Recherche dans tous les produits
            searchResults = allProduits.stream()
                    .filter(p -> p.getNom().toLowerCase().contains(searchTerm) ||
                            p.getDescription().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());

            lblResultInfo.setText("Résultats de recherche pour \"" + searchTerm + "\" (" + searchResults.size() + ")");
        }

        displayProduits(searchResults);
    }

    @FXML
    private void handleRetourCategories() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/front/categorie.fxml")));
            Stage stage = (Stage) btnRetourCategories.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers les catégories: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetourAccueil() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/front/accueil.fxml"));
            Stage stage = (Stage) btnAccueil.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers l'accueil: " + e.getMessage());
        }
    }
    public void setCategorieId(int categoryId) {
        loadProductsByCategory(categoryId);
    }
    @FXML
    private void handleVoiceSearch() {
        if (!isRecording) {
            startVoiceRecognition();
        } else {
            stopVoiceRecognition();
        }
    }

    private void startVoiceRecognition() {
        voiceRecognitionOverlay.setVisible(true);
        voicePromptLabel.setText("Dites le nom du produit à rechercher...");

        executorService.submit(() -> {
            try {
                // Audio format configuration
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    Platform.runLater(() -> {
                        voicePromptLabel.setText("Le microphone n'est pas supporté.");
                        showAlert("Erreur", "Votre système ne supporte pas l'entrée audio.");
                    });
                    return;
                }

                // Get and start the microphone capture line
                microphoneLine = (TargetDataLine) AudioSystem.getLine(info);
                microphoneLine.open(format);
                microphoneLine.start();

                isRecording = true;

                // Create a temporary file for the audio
                File audioFile = File.createTempFile("voice_search", ".wav");

                // Record audio to file
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];

                try {
                    while (isRecording) {
                        int count = microphoneLine.read(buffer, 0, buffer.length);
                        if (count > 0) {
                            out.write(buffer, 0, count);
                        }
                    }

                    // Convert to audio file
                    byte[] audioData = out.toByteArray();
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                         AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length)) {
                        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);
                    }

                    // Here, you would typically send the audio file to a speech recognition service
                    // For demonstration, we'll simulate a result
                    simulateVoiceRecognitionResult(audioFile);

                } finally {
                    if (microphoneLine != null) {
                        microphoneLine.close();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    voicePromptLabel.setText("Erreur lors de l'enregistrement: " + e.getMessage());
                    showAlert("Erreur", "Erreur lors de l'enregistrement audio: " + e.getMessage());
                });
            } finally {
                Platform.runLater(() -> {
                    voiceRecognitionOverlay.setVisible(false);
                });
            }
        });
    }

    private void simulateVoiceRecognitionResult(File audioFile) {
        // In a real implementation, you would send this file to a speech-to-text service
        // For now, we'll simulate a delay and then use a mock result

        try {
            Thread.sleep(2000); // Simulate processing time

            // For demo, let's pretend we recognized "smartphone"
            String recognizedText = "smartphone";

            Platform.runLater(() -> {
                searchField.setText(recognizedText);
                handleSearch();
                voicePromptLabel.setText("Recherche pour: " + recognizedText);

                // In a real implementation, you would integrate with a speech recognition API
                // such as Google Cloud Speech-to-Text, IBM Watson, or Mozilla DeepSpeech

                showAlert("Reconnaissance vocale",
                        "Texte reconnu: " + recognizedText);
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopVoiceRecognition() {
        isRecording = false;
        if (microphoneLine != null) {
            microphoneLine.stop();
            microphoneLine.close();
        }
    }

    @FXML
    private void cancelVoiceRecognition() {
        stopVoiceRecognition();
        voiceRecognitionOverlay.setVisible(false);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}