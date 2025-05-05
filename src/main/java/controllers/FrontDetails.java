package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Category;
import models.Produit;
import services.CategorieService;
import services.ProduitService;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FrontDetails implements Initializable {

    @FXML private ImageView qrCodeImageView;
    @FXML private Label produitInitialesLabel;
    @FXML private Label categorieLabel;
    @FXML private Label nomLabel;
    @FXML private Label prixLabel;
    @FXML private Label stockLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label caracteristiquesLabel;
    @FXML private Label utilisationLabel;
    @FXML private Button btnAddToCart;
    @FXML private Button btnView3D;
    @FXML private Button btnSaveQR;
    @FXML private HBox produitsSimilairesBox;
    @FXML private StackPane productImageContainer;

    private Produit produit;
    private ProduitService produitService;
    private CategorieService categorieService;
    private int produitId;
    private WritableImage qrCodeImage;
    private final DecimalFormat df = new DecimalFormat("0.00");

    // Constants for image handling
    private static final String IMAGE_BASE_PATH = "/images/produits/";
    private static final double IMAGE_WIDTH = 300;
    private static final double IMAGE_HEIGHT = 200;

    private final String[] colorPalette = {
            "#3498db", "#2ecc71", "#9b59b6", "#e74c3c", "#f39c12",
            "#1abc9c", "#d35400", "#16a085", "#8e44ad", "#c0392b"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produitService = ProduitService.getInstance();
        categorieService = new CategorieService();

        // Initialize productImageContainer if it's null
        if (productImageContainer == null) {
            productImageContainer = new StackPane();
            productImageContainer.setPrefHeight(IMAGE_HEIGHT);
            productImageContainer.setPrefWidth(IMAGE_WIDTH);
            productImageContainer.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            productImageContainer.setPadding(new Insets(10));
        }
    }

    public void setProduitId(int id) {
        this.produitId = id;
        chargerProduit();
    }

    private void chargerProduit() {
        produit = produitService.getById(produitId);
        if (produit != null) {
            afficherDetailsProduit();
            chargerProduitsSimilaires();
        } else {
            afficherErreur("Produit introuvable", "Le produit demandé n'existe pas ou a été supprimé.");
        }
    }

    private ImageView loadProductImage(Produit produit) {
        String imagePath = null;
        InputStream inputStream = null;

        // 1. Chemin personnalisé
        if (produit.getimage_path() != null && !produit.getimage_path().isEmpty()) {
            imagePath = produit.getimage_path();
            inputStream = getClass().getResourceAsStream(imagePath);
        }

        // 2. Nom automatique
        if (inputStream == null) {
            String fileName = produit.getNom().toLowerCase().replaceAll("\\s+", "_");
            imagePath = "/images/produits/" + fileName + ".jpg";
            inputStream = getClass().getResourceAsStream(imagePath);
            if (inputStream == null) {
                imagePath = "/images/produits/" + fileName + ".png";
                inputStream = getClass().getResourceAsStream(imagePath);
            }
        }

        // 3. Fallback image 1.png
        if (inputStream == null) {
            imagePath = "/images/produits/1.png";
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
        return null;
    }

    private void afficherDetailsProduit() {
        // Ensure productImageContainer is initialized
        if (productImageContainer == null) {
            productImageContainer = new StackPane();
            productImageContainer.setPrefHeight(IMAGE_HEIGHT);
            productImageContainer.setPrefWidth(IMAGE_WIDTH);
            productImageContainer.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            productImageContainer.setPadding(new Insets(10));
        }

        // Load and display product image
        ImageView productImage = loadProductImage(produit);
        productImageContainer.getChildren().clear();

        if (productImage != null) {
            // Add rounded corner effect to image
            Rectangle clip = new Rectangle(IMAGE_WIDTH, IMAGE_HEIGHT);
            clip.setArcWidth(15);
            clip.setArcHeight(15);
            productImage.setClip(clip);
            productImageContainer.getChildren().add(productImage);
        } else {
            // Fallback to initials system if no image is available
            String randomColor = colorPalette[Math.abs(produit.getNom().hashCode()) % colorPalette.length];
            Rectangle rectangle = new Rectangle(IMAGE_WIDTH, IMAGE_HEIGHT);
            rectangle.setFill(Color.web(randomColor));
            rectangle.setArcWidth(15);
            rectangle.setArcHeight(15);

            Label initialsLabel = new Label(getInitiales(produit.getNom()));
            initialsLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
            initialsLabel.setTextFill(Color.WHITE);
            initialsLabel.setTextAlignment(TextAlignment.CENTER);

            productImageContainer.getChildren().addAll(rectangle, initialsLabel);
        }

        // Informations de base
        nomLabel.setText(produit.getNom());
        prixLabel.setText(df.format(produit.getPrix()) + " €");
        descriptionLabel.setText(produit.getDescription());
        produitInitialesLabel.setText(getInitiales(produit.getNom()));

        // Gestion du stock
        if (produit.getQuantite() > 0) {
            stockLabel.setText("En stock (" + produit.getQuantite() + ")");
            stockLabel.getStyleClass().remove("stock-indisponible");
            stockLabel.getStyleClass().add("stock-disponible");
            btnAddToCart.setDisable(false);
        } else {
            stockLabel.setText("Rupture de stock");
            stockLabel.getStyleClass().remove("stock-disponible");
            stockLabel.getStyleClass().add("stock-indisponible");
            btnAddToCart.setDisable(true);
        }

        // Catégorie
        Category categorie = categorieService.getById(produit.getCategorie());
        if (categorie != null) {
            categorieLabel.setText(categorie.getName());
        } else {
            categorieLabel.setText("Catégorie inconnue");
        }

        // Caractéristiques et utilisation
        String[] parties = produit.getDescription().split("\\|");
        if (parties.length > 1) {
            caracteristiquesLabel.setText(parties[1].trim());
        } else {
            caracteristiquesLabel.setText("Caractéristiques non spécifiées");
        }

        if (parties.length > 2) {
            utilisationLabel.setText(parties[2].trim());
        } else {
            utilisationLabel.setText("Instructions d'utilisation non spécifiées");
        }

        // Modèle 3D
        btnView3D.setDisable(produit.getModel3D() == null || produit.getModel3D().isEmpty());

        // Générer le QR code
        generateQRCode();
    }

    private void generateQRCode() {
        // Afficher l'image 1.png à la place du QR code
        InputStream inputStream = getClass().getResourceAsStream("/images/produits/1.png");
        if (inputStream != null) {
            Image image = new Image(inputStream);
            qrCodeImageView.setImage(image);
        }
    }

    private void chargerProduitsSimilaires() {
        List<Produit> produitsSimilaires = produitService.getAll().stream()
                .filter(p -> p.getCategorie() == produit.getCategorie() && p.getId() != produit.getId())
                .limit(4)
                .toList();

        produitsSimilairesBox.getChildren().clear();
        for (Produit p : produitsSimilaires) {
            produitsSimilairesBox.getChildren().add(creerCarteProduit(p));
        }
    }

    private VBox creerCarteProduit(Produit p) {
        VBox card = new VBox();
        card.getStyleClass().add("produit-card");
        card.setPrefWidth(230);
        card.setPrefHeight(140);
        card.setSpacing(5);
        card.setPadding(new Insets(10));

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(80);
        imageContainer.getStyleClass().add("image-container");

        // Try to load product image
        ImageView productImage = loadProductImage(p);
        if (productImage != null) {
            // Add rounded corner effect to image
            Rectangle clip = new Rectangle(IMAGE_WIDTH, IMAGE_HEIGHT);
            clip.setArcWidth(10);
            clip.setArcHeight(10);
            productImage.setClip(clip);
            imageContainer.getChildren().add(productImage);
        } else {
            // Fallback to initials
            String randomColor = colorPalette[Math.abs(p.getNom().hashCode()) % colorPalette.length];
            Rectangle rectangle = new Rectangle(IMAGE_WIDTH, IMAGE_HEIGHT);
            rectangle.setFill(Color.web(randomColor));
            rectangle.setArcWidth(10);
            rectangle.setArcHeight(10);

            Label initiales = new Label(getInitiales(p.getNom()));
            initiales.setFont(Font.font("System", FontWeight.BOLD, 24));
            initiales.setTextFill(Color.WHITE);
            initiales.setTextAlignment(TextAlignment.CENTER);

            imageContainer.getChildren().addAll(rectangle, initiales);
        }

        Label nom = new Label(p.getNom());
        nom.getStyleClass().add("produit-nom");

        Label prix = new Label(df.format(p.getPrix()) + " €");
        prix.getStyleClass().add("produit-prix");

        Button btnVoir = new Button("Voir détails");
        btnVoir.getStyleClass().addAll("btn-secondary", "btn-sm");
        btnVoir.setOnAction(e -> afficherDetailsProduit(p.getId()));

        card.getChildren().addAll(imageContainer, nom, prix, btnVoir);
        card.setOnMouseClicked(e -> afficherDetailsProduit(p.getId()));

        return card;
    }

    @FXML
    private void saveQRCode() {
        if (qrCodeImage == null) {
            afficherErreur("Erreur", "Aucun QR code à sauvegarder");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le QR Code");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );
        fileChooser.setInitialFileName(produit.getNom() + "_QRCode.png");

        File file = fileChooser.showSaveDialog(qrCodeImageView.getScene().getWindow());
        if (file != null) {
            try {
                // Create a BufferedImage from the QR code
                BufferedImage bufferedImage = new BufferedImage(
                        (int) qrCodeImage.getWidth(),
                        (int) qrCodeImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );

                // Copy pixel data
                for (int x = 0; x < qrCodeImage.getWidth(); x++) {
                    for (int y = 0; y < qrCodeImage.getHeight(); y++) {
                        javafx.scene.paint.Color color = qrCodeImage.getPixelReader().getColor(x, y);
                        int argb = (int)(color.getOpacity() * 255) << 24 |
                                (int)(color.getRed() * 255) << 16 |
                                (int)(color.getGreen() * 255) << 8 |
                                (int)(color.getBlue() * 255);
                        bufferedImage.setRGB(x, y, argb);
                    }
                }

                // Save the image
                ImageIO.write(bufferedImage, "png", file);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("QR Code sauvegardé avec succès!");
                alert.showAndWait();
            } catch (IOException e) {
                afficherErreur("Erreur", "Impossible de sauvegarder le QR code: " + e.getMessage());
            }
        }
    }

    @FXML
    private void addToCart() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Panier");
        alert.setHeaderText("Produit ajouté au panier");
        alert.setContentText("Le produit " + produit.getNom() + " a été ajouté à votre panier.");
        alert.showAndWait();
    }

    @FXML
    private void view3DModel() {
        if (produit.getModel3D() != null && !produit.getModel3D().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/model3d_viewer.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Visualisation 3D - " + produit.getNom());
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                afficherErreur("Erreur de visualisation", "Impossible d'afficher le modèle 3D: " + e.getMessage());
            }
        } else {
            afficherErreur("Modèle 3D non disponible", "Ce produit n'a pas de modèle 3D associé.");
        }
    }

    @FXML
    private void retourProduits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit.fxml"));
            Parent root = loader.load();
            FrontProduit controller = loader.getController();
            controller.setCategorieId(produit.getCategorie());
            Scene scene = btnAddToCart.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            afficherErreur("Erreur de navigation", "Impossible de retourner à la liste des produits: " + e.getMessage());
        }
    }

    @FXML
    private void allerAccueil() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/front/accueil.fxml")));
            Scene scene = btnAddToCart.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            afficherErreur("Erreur de navigation", "Impossible d'aller à l'accueil: " + e.getMessage());
        }
    }

    private String getInitiales(String nom) {
        if (nom == null || nom.isEmpty()) return "XX";

        String[] mots = nom.split(" ");
        StringBuilder initiales = new StringBuilder();

        for (String mot : mots) {
            if (!mot.isEmpty()) {
                initiales.append(mot.charAt(0));
                if (initiales.length() >= 2) break;
            }
        }

        while (initiales.length() < 2) {
            initiales.append("X");
        }

        return initiales.toString().toUpperCase();
    }

    private void afficherDetailsProduit(int id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/details.fxml"));
            Parent root = loader.load();
            FrontDetails controller = loader.getController();
            controller.setProduitId(id);
            Scene scene = btnAddToCart.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            afficherErreur("Erreur de navigation", "Impossible d'afficher les détails du produit: " + e.getMessage());
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}