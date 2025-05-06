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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.application.Platform;
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
    private TextField minPriceField;

    @FXML
    private TextField maxPriceField;

    @FXML
    private Slider priceRangeSlider;

    @FXML
    private Label priceRangeLabel;

    @FXML
    private Button btnResetPrice;

    private ProduitService produitService;
    private ExecutorService executorService;
    private boolean isRecording = false;
    private TargetDataLine microphoneLine;
    private CategorieService categorieService;
    private List<Produit> allProduits;
    private Integer selectedCategoryId = null;

    private double currentMaxPrice = 100.0;
    private double currentMinPrice = 0.0;

    // Constants for image handling
    private static final String IMAGE_BASE_PATH = "/images/produits/";
    private static final double IMAGE_WIDTH = 180;
    private static final double IMAGE_HEIGHT = 120;

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

        loadCategories();
        loadAllProduits();
        setupPriceFiltering();

        categorieComboBox.setOnAction(event -> {
            Category selectedCategory = categorieComboBox.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                loadProductsByCategory(selectedCategory.getId());
            } else {
                loadAllProduits();
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
    }

    private void setupPriceFiltering() {
        // Setup price range slider
        priceRangeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentMaxPrice = newVal.doubleValue();
            updatePriceRangeLabel();
            handleSearch();
        });

        // Setup min price field
        minPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double value = Double.parseDouble(newVal);
                if (value >= 0 && value <= currentMaxPrice) {
                    currentMinPrice = value;
                    updatePriceRangeLabel();
                    handleSearch();
                }
            } catch (NumberFormatException e) {
                // Invalid input, ignore
            }
        });

        // Setup max price field
        maxPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double value = Double.parseDouble(newVal);
                if (value >= currentMinPrice) {
                    currentMaxPrice = value;
                    priceRangeSlider.setValue(value);
                    updatePriceRangeLabel();
                    handleSearch();
                }
            } catch (NumberFormatException e) {
                // Invalid input, ignore
            }
        });

        // Initial price range label
        updatePriceRangeLabel();
    }

    private void updatePriceRangeLabel() {
        priceRangeLabel.setText(String.format("Gamme de prix: %.2f€ - %.2f€", currentMinPrice, currentMaxPrice));
    }

    @FXML
    private void resetPriceFilter() {
        currentMinPrice = 0.0;
        currentMaxPrice = 100.0;
        minPriceField.clear();
        maxPriceField.clear();
        priceRangeSlider.setValue(100.0);
        updatePriceRangeLabel();
        handleSearch();
    }

    private void loadCategories() {
        List<Category> categories = categorieService.getAll();
        categorieComboBox.getItems().clear();
        categorieComboBox.getItems().addAll(categories);
        categorieComboBox.getItems().addFirst(new Category(0, "Toutes les catégories", ""));
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

        for (Category cat : categorieComboBox.getItems()) {
            if (cat.getId() == categoryId) {
                categorieComboBox.getSelectionModel().select(cat);
                break;
            }
        }

        List<Produit> filteredProduits = allProduits.stream()
                .filter(p -> p.getCategorie() == categoryId)
                .collect(Collectors.toList());

        displayProduits(filteredProduits);

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

    private ImageView loadProductImage(Produit produit) {
        try {
            String imagePath = null;

            // First try to use the product's image_path if it exists
            if (produit.getimage_path() != null && !produit.getimage_path().isEmpty()) {
                imagePath = produit.getimage_path();
            } else {
                // Otherwise try to find image based on product name
                String fileName = produit.getNom().toLowerCase().replaceAll("\\s+", "_") + ".jpg";
                imagePath = IMAGE_BASE_PATH + fileName;
            }

            // Try to load the image
            InputStream inputStream = getClass().getResourceAsStream(imagePath);
            if (inputStream == null) {
                // Try with .png extension
                imagePath = imagePath.replace(".jpg", ".png");
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

            return null; // Image not found
        } catch (Exception e) {
            System.err.println("Could not load image for product: " + produit.getNom());
            return null;
        }
    }

    private VBox createProduitCard(Produit produit) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3);");
        card.setPrefWidth(220);
        card.setPrefHeight(300);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefHeight(IMAGE_HEIGHT);
        iconContainer.setPrefWidth(IMAGE_WIDTH);

        // Try to load product image
        ImageView productImage = loadProductImage(produit);

        if (productImage != null) {
            // Add rounded corner effect to image
            Rectangle clip = new Rectangle(IMAGE_WIDTH, IMAGE_HEIGHT);
            clip.setArcWidth(15);
            clip.setArcHeight(15);
            productImage.setClip(clip);
            iconContainer.getChildren().add(productImage);
        } else {
            // Fallback to initials system if no image is available
            String randomColor = colorPalette[Math.abs(produit.getNom().hashCode()) % colorPalette.length];
            Rectangle rectangle = new Rectangle(IMAGE_WIDTH, IMAGE_HEIGHT);
            rectangle.setFill(Color.web(randomColor));
            rectangle.setArcWidth(15);
            rectangle.setArcHeight(15);

            String initialLetters = getInitials(produit.getNom());
            Label initialsLabel = new Label(initialLetters);
            initialsLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
            initialsLabel.setTextFill(Color.WHITE);
            initialsLabel.setTextAlignment(TextAlignment.CENTER);

            iconContainer.getChildren().addAll(rectangle, initialsLabel);
        }

        // Product name
        Label nameLabel = new Label(produit.getNom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#2c3e50"));
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setMaxWidth(180);

        // Product price
        Label priceLabel = new Label(String.format("%.2f €", produit.getPrix()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        priceLabel.setTextFill(Color.web("#e74c3c"));

        // Show availability
        HBox availabilityBox = new HBox();
        availabilityBox.setAlignment(Pos.CENTER);
        availabilityBox.setSpacing(5);

        Circle statusCircle = new Circle(5);
        statusCircle.setFill(produit.getQuantite() > 0 ? Color.web("#2ecc71") : Color.web("#e74c3c"));

        Label statusLabel = new Label(produit.getQuantite() > 0 ? "En stock" : "Rupture de stock");
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setTextFill(produit.getQuantite() > 0 ? Color.web("#2ecc71") : Color.web("#e74c3c"));

        availabilityBox.getChildren().addAll(statusCircle, statusLabel);

        // Button to view product details
        Button viewDetailsBtn = new Button("Voir détails");
        viewDetailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-cursor: hand;");
        viewDetailsBtn.setPrefWidth(150);
        viewDetailsBtn.setOnAction(event -> viewProductDetails(produit.getId()));

        // Add hover effect to button
        viewDetailsBtn.setOnMouseEntered(e ->
                viewDetailsBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;")
        );
        viewDetailsBtn.setOnMouseExited(e ->
                viewDetailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;")
        );

        // Add all elements to card
        card.getChildren().addAll(iconContainer, nameLabel, priceLabel, availabilityBox, viewDetailsBtn);

        // Add hover effect to entire card
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
        card.setOnMouseClicked(event -> viewProductDetails(produit.getId()));

        return card;
    }

    private static class Circle extends javafx.scene.shape.Circle {
        public Circle(double radius) {
            super(radius);
        }
    }

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

    private void viewProductDetails(int produitId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/details.fxml"));
            Parent root = loader.load();

            FrontDetails controller = loader.getController();
            controller.setProduitId(produitId);

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

        List<Produit> filteredProducts = allProduits.stream()
                .filter(p -> p.getPrix() >= currentMinPrice && p.getPrix() <= currentMaxPrice)
                .filter(p -> {
                    if (searchTerm.isEmpty()) return true;
                    return p.getNom().toLowerCase().contains(searchTerm) ||
                            p.getDescription().toLowerCase().contains(searchTerm);
                })
                .filter(p -> {
                    if (selectedCategoryId == null || selectedCategoryId == 0) return true;
                    return p.getCategorie() == selectedCategoryId;
                })
                .collect(Collectors.toList());

        displayProduits(filteredProducts);
        updateResultInfo(filteredProducts.size(), searchTerm);
    }

    private void updateResultInfo(int resultCount, String searchTerm) {
        StringBuilder info = new StringBuilder();

        if (!searchTerm.isEmpty()) {
            info.append("Résultats pour \"").append(searchTerm).append("\"");
        }

        if (selectedCategoryId != null && selectedCategoryId != 0) {
            Category category = categorieService.getById(selectedCategoryId);
            if (category != null) {
                if (!info.isEmpty()) info.append(" dans ");
                info.append("Catégorie: ").append(category.getName());
            }
        }

        if (currentMinPrice > 0 || currentMaxPrice < 100) {
            if (!info.isEmpty()) info.append(" | ");
            info.append(String.format("Prix: %.2f€ - %.2f€", currentMinPrice, currentMaxPrice));
        }

        if (!info.isEmpty()) {
            info.append(" (").append(resultCount).append(" produit(s))");
        } else {
            info.append("Affichage de tous les produits (").append(resultCount).append(")");
        }

        lblResultInfo.setText(info.toString());
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
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/front/accueil.fxml")));
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
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    Platform.runLater(() -> {
                        voicePromptLabel.setText("Le microphone n'est pas supporté.");
                        showAlert("Erreur", "Votre système ne supporte pas l'entrée audio.");
                    });
                    return;
                }

                microphoneLine = (TargetDataLine) AudioSystem.getLine(info);
                microphoneLine.open(format);
                microphoneLine.start();

                isRecording = true;

                File audioFile = File.createTempFile("voice_search", ".wav");

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

                    byte[] audioData = out.toByteArray();
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                         AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length)) {
                        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);
                    }

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
        try {
            Thread.sleep(2000);

            String recognizedText = "smartphone";

            Platform.runLater(() -> {
                searchField.setText(recognizedText);
                handleSearch();
                voicePromptLabel.setText("Recherche pour: " + recognizedText);

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