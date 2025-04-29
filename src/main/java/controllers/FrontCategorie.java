package controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import models.Category;
import services.CategorieService;
import services.ProduitService;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FrontCategorie {

    @FXML
    private FlowPane categoriesContainer;
    @FXML
    private Button btnRetour;
    @FXML
    private Label lblTopCategory;
    @FXML
    private TextField searchField;

    @FXML
    private Button voiceSearchButton;

    @FXML
    private Button searchButton;

    @FXML
    private StackPane voiceRecognitionOverlay;

    @FXML
    private Label voicePromptLabel;

    @FXML
    private TableView<Category> categoriesTable; // Assuming you have this

    private CategorieService categorieService;
    private ExecutorService executorService;
    private boolean isRecording = false;
    private TargetDataLine microphoneLine;
    private static final Logger LOGGER = Logger.getLogger(FrontCategorie.class.getName());

    // Chemin de base pour les images de catégories
    private static final String IMAGE_BASE_PATH = "/images/categories/";

    // Dimension des images
    private static final double IMAGE_WIDTH = 180;
    private static final double IMAGE_HEIGHT = 100;

    // Palette de couleurs pour les cartes de catégorie (fallback)
    private final String[] colorPalette = {
            "#3498db", "#2ecc71", "#9b59b6", "#e74c3c", "#f39c12",
            "#1abc9c", "#d35400", "#16a085", "#8e44ad", "#c0392b"
    };
    private final Random random = new Random();

    @FXML
    public void initialize() {
        categorieService = new CategorieService();
        executorService = Executors.newSingleThreadExecutor();

        // Populate your table with all categories
        loadCategories();

        // Add listener to search field for real-time filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCategories(newValue);
        });
        loadTopCategoryStats();
    }

    /**
     * Charge les statistiques de la catégorie la plus vendue
     */
    private void loadTopCategoryStats() {
        try {
            String topCategory = ProduitService.getInstance().getMostSoldCategory();
            if (topCategory == null || topCategory.isEmpty() || topCategory.contains("Erreur")) {
                displayErrorStats("Statistiques indisponibles");
            } else {
                lblTopCategory.setText("🔥 Catégorie la plus populaire : " + topCategory);
                lblTopCategory.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5px 15px; -fx-background-radius: 15px;");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement des statistiques", e);
            displayErrorStats("Données temporairement indisponibles");
        }
    }

    /**
     * Affiche un message d'erreur pour les statistiques
     */
    private void displayErrorStats(String message) {
        lblTopCategory.setText(message);
        lblTopCategory.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-padding: 5px 15px; -fx-background-radius: 15px;");
    }

    /**
     * Charge toutes les catégories depuis le service
     */
    private void loadCategories() {
        try {
            List<Category> categories = categorieService.getAll();
            categoriesContainer.getChildren().clear();

            for (Category category : categories) {
                VBox categoryCard = createCategoryCard(category);
                categoriesContainer.getChildren().add(categoryCard);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
            showError("Impossible de charger les catégories");
        }
    }

    /**
     * Affiche une erreur dans l'interface
     */
    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        categoriesContainer.getChildren().clear();
        categoriesContainer.getChildren().add(errorLabel);
    }

    /**
     * Génère les initiales à partir du nom de la catégorie
     */
    private String getInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "??";
        }

        StringBuilder initials = new StringBuilder();
        String[] words = name.split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
                if (initials.length() >= 2) break;
            }
        }

        if (initials.length() == 1 && words[0].length() > 1) {
            initials.append(Character.toUpperCase(words[0].charAt(1)));
        }

        return initials.toString();
    }

    /**
     * Tente de charger une image pour la catégorie
     * @param category La catégorie pour laquelle charger une image
     * @return Un ImageView contenant l'image ou null si l'image n'est pas trouvée
     */
    private ImageView loadCategoryImage(Category category) {
        try {
            // Essayer de charger l'image depuis le chemin spécifique à la catégorie
            String imagePath = null;

            // Si la catégorie a un chemin d'image spécifié
            if (category.getimage_path() != null && !category.getimage_path().isEmpty()) {
                imagePath = category.getimage_path();
            } else {
                // Sinon, essayer de trouver une image basée sur le nom de la catégorie
                // Convertir le nom en un format de fichier valide (sans espaces, minuscules)
                String fileName = category.getName().toLowerCase().replaceAll("\\s+", "_") + ".png";
                imagePath = IMAGE_BASE_PATH + fileName;
            }

            // Tenter de charger l'image
            InputStream inputStream = getClass().getResourceAsStream(imagePath);
            if (inputStream == null) {
                // Essayer une autre extension
                imagePath = imagePath.replace(".png", ".jpg");
                inputStream = getClass().getResourceAsStream(imagePath);
            }

            if (inputStream != null) {
                Image image = new Image(inputStream);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(IMAGE_WIDTH);
                imageView.setFitHeight(IMAGE_HEIGHT);
                imageView.setPreserveRatio(true);
                return imageView;
            }

            return null; // Image non trouvée
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Impossible de charger l'image pour la catégorie: " + category.getName(), e);
            return null;
        }
    }

    /**
     * Crée une carte visuelle pour représenter une catégorie
     */
    private VBox createCategoryCard(Category category) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3);");
        card.setPrefWidth(220);
        card.setPrefHeight(180);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefHeight(100);
        iconContainer.setPrefWidth(180);

        // Essayer de charger l'image
        ImageView categoryImage = loadCategoryImage(category);

        if (categoryImage != null) {
            // Ajouter un effet de bord arrondi à l'image
            Rectangle clip = new Rectangle(180, 100);
            clip.setArcWidth(15);
            clip.setArcHeight(15);
            categoryImage.setClip(clip);

            iconContainer.getChildren().add(categoryImage);
        } else {
            // Fallback au système d'initiales si aucune image n'est disponible
            String randomColor = colorPalette[random.nextInt(colorPalette.length)];
            Rectangle rectangle = new Rectangle(180, 100);
            rectangle.setFill(Color.web(randomColor));
            rectangle.setArcWidth(15);
            rectangle.setArcHeight(15);

            String initialLetters = getInitials(category.getName());
            Label initialsLabel = new Label(initialLetters);
            initialsLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
            initialsLabel.setTextFill(Color.WHITE);
            initialsLabel.setTextAlignment(TextAlignment.CENTER);

            iconContainer.getChildren().addAll(rectangle, initialsLabel);
        }

        Label nameLabel = new Label(category.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#2c3e50"));
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setMaxWidth(180);

        Button viewProductsBtn = new Button("Voir les produits");
        viewProductsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-cursor: hand;");
        viewProductsBtn.setPrefWidth(150);
        viewProductsBtn.setOnAction(event -> viewProductsByCategory(category.getId()));

        // Effet de survol sur le bouton
        viewProductsBtn.setOnMouseEntered(e ->
                viewProductsBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;")
        );
        viewProductsBtn.setOnMouseExited(e ->
                viewProductsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;")
        );

        card.getChildren().addAll(iconContainer, nameLabel, viewProductsBtn);

        // Effet de survol sur toute la carte
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #3498db; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8; " +
                        "-fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(52,152,219,0.3), 10, 0, 0, 5); " +
                        "-fx-cursor: hand;")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8; " +
                        "-fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3);")
        );
        card.setOnMouseClicked(event -> viewProductsByCategory(category.getId()));

        return card;
    }

    /**
     * Navigue vers la page des produits filtrés par catégorie
     */
    private void viewProductsByCategory(int categoryId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit.fxml"));
            Parent root = loader.load();

            FrontProduit frontProduit = loader.getController();
            frontProduit.loadProductsByCategory(categoryId);

            Stage stage = (Stage) categoriesContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur de navigation vers produits", e);
            showError("Impossible d'afficher les produits de cette catégorie");
        }
    }

    /**
     * Gère le retour à la page d'accueil
     */
    @FXML
    private void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/front/accueil.fxml"));
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur de navigation vers accueil", e);
            showError("Impossible de retourner à l'accueil");
        }
    }
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        filterCategories(searchText);
    }

    private void filterCategories(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadCategories(); // Reset to all categories
            return;
        }

        try {
            List<Category> filteredList = categorieService.getAll().stream()
                    .filter(category -> category.getName().toLowerCase().contains(searchText.toLowerCase()))
                    .toList();

            categoriesContainer.getChildren().clear();

            if (filteredList.isEmpty()) {
                Label noResultsLabel = new Label("Aucune catégorie trouvée pour: " + searchText);
                noResultsLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");
                categoriesContainer.getChildren().add(noResultsLabel);
            } else {
                for (Category category : filteredList) {
                    VBox categoryCard = createCategoryCard(category);
                    categoriesContainer.getChildren().add(categoryCard);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du filtrage des catégories", e);
            showError("Impossible de filtrer les catégories");
        }
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
        voicePromptLabel.setText("Dites le nom de la catégorie à rechercher...");

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

            // For demo, let's pretend we recognized "électronique"
            String recognizedText = "électronique";

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