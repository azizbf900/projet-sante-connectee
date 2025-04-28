package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Category;
import models.Produit;
import services.CategorieService;
import services.ProduitService;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

public class EditProduct implements Initializable {

    @FXML private Button backButton;
    @FXML private Label productIdLabel;

    @FXML private TextField nameField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextArea descriptionArea;

    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private Button decrementButton;
    @FXML private Button incrementButton;
    @FXML private TextField thresholdField;
    @FXML private Label statusLabel;

    @FXML private Button addAttributeButton;
    @FXML private VBox attributesContainer;

    @FXML private Label validationMessage;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    @FXML private StackPane loadingOverlay;
    @FXML private VBox notificationArea;

    // Services
    private final ProduitService produitService = ProduitService.getInstance();
    private final CategorieService categorieService = new CategorieService();

    // Current product
    private Produit currentProduct;

    // List to store product attributes
    private final Map<String, String> productAttributes = new HashMap<>();

    // Decimal formatter for price
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize loading overlay
        loadingOverlay.setVisible(false);

        // Setup category combo box
        setupCategoryComboBox();

        // Setup numeric fields
        setupNumericFields();

        // Setup event handlers
        setupEventHandlers();

        // Load CSS
        Platform.runLater(() -> {
            if (nameField.getScene() != null) {
                String css = Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm();
                nameField.getScene().getStylesheets().add(css);
            }
        });
    }

    /**
     * Method to load product data for editing
     * @param productId the ID of the product to edit
     */
    public void loadProductData(int productId) {
        loadingOverlay.setVisible(true);

        // Load product data
        currentProduct = produitService.getById(productId);

        if (currentProduct != null) {
            // Update product ID label
            productIdLabel.setText("ID: #" + currentProduct.getId());

            // Populate fields
            nameField.setText(currentProduct.getNom());
            descriptionArea.setText(currentProduct.getDescription());

            // Select category
            for (Category category : categoryComboBox.getItems()) {
                if (category.getId() == currentProduct.getCategorie()) {
                    categoryComboBox.setValue(category);
                    break;
                }
            }

            // Set price
            priceField.setText(decimalFormat.format(currentProduct.getPrix()));

            // Set quantity
            quantityField.setText(String.valueOf(currentProduct.getQuantite()));

            // Set threshold (assuming it exists in the product model)
            thresholdField.setText("10"); // Default value, replace with actual data if available

            // Update status label
            updateStatusLabel();

            // Load attributes (assuming there's a method to get them)
            // For demo purposes, we'll add some sample attributes
            addSampleAttributes();
        } else {
            showNotification("Erreur: Produit introuvable");
            navigateBack();
        }

        loadingOverlay.setVisible(false);
    }

    private void setupCategoryComboBox() {
        // Load categories
        List<Category> categories = categorieService.getAll();

        // Create observable list
        ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);

        // Set items
        categoryComboBox.setItems(categoryList);

        // Set converter to display category name
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return categoryComboBox.getItems().stream()
                        .filter(category -> category.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void setupNumericFields() {
        // Setup price field to only accept numbers and decimal point
        Pattern pattern = Pattern.compile("\\d*\\.?\\d*");
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!pattern.matcher(newValue).matches()) {
                priceField.setText(oldValue);
            }
        });

        // Setup quantity field to only accept integers
        Pattern intPattern = Pattern.compile("\\d*");
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!intPattern.matcher(newValue).matches()) {
                quantityField.setText(oldValue);
            } else {
                updateStatusLabel();
            }
        });

        // Setup threshold field to only accept integers
        thresholdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!intPattern.matcher(newValue).matches()) {
                thresholdField.setText(oldValue);
            }
        });

        // Setup increment/decrement buttons
        decrementButton.setOnAction(event -> {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity > 0) {
                quantityField.setText(String.valueOf(quantity - 1));
                updateStatusLabel();
            }
        });

        incrementButton.setOnAction(event -> {
            int quantity = Integer.parseInt(quantityField.getText());
            quantityField.setText(String.valueOf(quantity + 1));
            updateStatusLabel();
        });
    }

    private void setupEventHandlers() {
        // Back button
        backButton.setOnAction(event -> navigateBack());

        // Cancel button
        cancelButton.setOnAction(event -> showConfirmationDialog());

        // Save button
        saveButton.setOnAction(event -> saveProduct());

        // Add attribute button
        addAttributeButton.setOnAction(event -> addAttributeRow("", ""));
    }

    private void updateStatusLabel() {
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            quantity = 0;
        }

        int threshold;
        try {
            threshold = Integer.parseInt(thresholdField.getText());
        } catch (NumberFormatException e) {
            threshold = 10; // Default threshold
        }

        if (quantity <= 0) {
            statusLabel.setText("Rupture");
            statusLabel.getStyleClass().clear();
            statusLabel.getStyleClass().add("status-value");
            statusLabel.getStyleClass().add("status-out-of-stock");
        } else if (quantity <= threshold) {
            statusLabel.setText("Stock faible");
            statusLabel.getStyleClass().clear();
            statusLabel.getStyleClass().add("status-value");
            statusLabel.getStyleClass().add("status-low-stock");
        } else {
            statusLabel.setText("En stock");
            statusLabel.getStyleClass().clear();
            statusLabel.getStyleClass().add("status-value");
            statusLabel.getStyleClass().add("status-in-stock");
        }
    }

    private void addAttributeRow(String name, String value) {
        HBox attributeRow = new HBox();
        attributeRow.setSpacing(15);
        attributeRow.getStyleClass().add("attribute-row");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom de l'attribut");
        nameField.setText(name);
        nameField.getStyleClass().addAll("form-field", "attribute-name");
        HBox.setHgrow(nameField, javafx.scene.layout.Priority.ALWAYS);

        TextField valueField = new TextField();
        valueField.setPromptText("Valeur");
        valueField.setText(value);
        valueField.getStyleClass().addAll("form-field", "attribute-value");
        HBox.setHgrow(valueField, javafx.scene.layout.Priority.ALWAYS);

        Button removeButton = new Button();
        removeButton.getStyleClass().add("remove-button");

        ImageView trashIcon = new ImageView();
        trashIcon.setFitHeight(16);
        trashIcon.setFitWidth(16);
        // In a real app, you should load the image from resources
        // trashIcon.setImage(new Image(getClass().getResourceAsStream("/images/trash.png")));
        removeButton.setGraphic(trashIcon);

        removeButton.setOnAction(event -> attributesContainer.getChildren().remove(attributeRow));

        attributeRow.getChildren().addAll(nameField, valueField, removeButton);
        attributesContainer.getChildren().add(attributeRow);
    }

    private void addSampleAttributes() {
        // Add sample attributes (replace with actual data in production)
        addAttributeRow("Couleur", "Rouge");
        addAttributeRow("Dimensions", "10x15x5 cm");
        addAttributeRow("Poids", "250g");
    }

    private void saveProduct() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        loadingOverlay.setVisible(true);

        try {
            // Update product data
            currentProduct.setNom(nameField.getText().trim());
            currentProduct.setDescription(descriptionArea.getText().trim());
            currentProduct.setCategorie(categoryComboBox.getValue().getId());
            currentProduct.setPrix(decimalFormat.parse(priceField.getText()).doubleValue());
            currentProduct.setQuantite(Integer.parseInt(quantityField.getText()));

            // Save product
            boolean success = produitService.updateProduit(currentProduct);

            if (success) {
                showNotification("Produit mis à jour avec succès");

                // Navigate back to product list after a short delay
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1)).play();
                Platform.runLater(this::navigateBack);
            } else {
                showNotification("Erreur lors de la mise à jour du produit");
            }

        } catch (ParseException e) {
            showNotification("Erreur de format: prix invalide");
        } finally {
            loadingOverlay.setVisible(false);
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // Check required fields
        if (nameField.getText().trim().isEmpty()) {
            errors.append("Le nom du produit est requis\n");
        }

        if (categoryComboBox.getValue() == null) {
            errors.append("La catégorie est requise\n");
        }

        if (priceField.getText().trim().isEmpty()) {
            errors.append("Le prix est requis\n");
        } else {
            try {
                double price = decimalFormat.parse(priceField.getText()).doubleValue();
                if (price < 0) {
                    errors.append("Le prix ne peut pas être négatif\n");
                }
            } catch (ParseException e) {
                errors.append("Format de prix invalide\n");
            }
        }

        if (quantityField.getText().trim().isEmpty()) {
            errors.append("La quantité est requise\n");
        } else {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity < 0) {
                    errors.append("La quantité ne peut pas être négative\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Format de quantité invalide\n");
            }
        }

        if (errors.length() > 0) {
            validationMessage.setText(errors.toString());
            validationMessage.setVisible(true);
            return false;
        } else {
            validationMessage.setVisible(false);
            return true;
        }
    }

    private void navigateBack() {
        try {
            // Load product list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listproduit.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) backButton.getScene().getWindow();

            // Set scene
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showNotification("Erreur lors du chargement de la liste des produits");
        }
    }

    private void showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler les modifications");
        alert.setContentText("Êtes-vous sûr de vouloir annuler les modifications ? Les changements non enregistrés seront perdus.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            navigateBack();
        }
    }

    private void showNotification(String message) {
        Label notification = new Label(message);
        notification.getStyleClass().add("notification");
        notificationArea.getChildren().add(notification);

        // Create fade transition
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                javafx.util.Duration.seconds(3), notification);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> notificationArea.getChildren().remove(notification));

        // Create pause transition before fade out
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(3));
        delay.setOnFinished(event -> fadeOut.play());

        delay.play();
    }
}