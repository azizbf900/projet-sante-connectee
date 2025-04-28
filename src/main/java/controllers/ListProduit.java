package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.Initializable;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.Category;
import models.Produit;
import services.CategorieService;
import services.ProduitService;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ListProduit implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button refreshButton;
    @FXML private Button addProductButton;

    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label totalValueLabel;

    @FXML private TableView<Produit> productTable;
    @FXML private TableColumn<Produit, Integer> idColumn;
    @FXML private TableColumn<Produit, String> nameColumn;
    @FXML private TableColumn<Produit, String> descriptionColumn;
    @FXML private TableColumn<Produit, String> categoryColumn;
    @FXML private TableColumn<Produit, Double> priceColumn;
    @FXML private TableColumn<Produit, Integer> quantityColumn;
    @FXML private TableColumn<Produit, String> statusColumn;
    @FXML private TableColumn<Produit, Void> actionsColumn;

    @FXML private Button deleteSelectedButton;
    @FXML private Label selectedItemsLabel;
    @FXML private Button firstPageButton;
    @FXML private Button previousPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private ComboBox<String> itemsPerPageCombo;

    @FXML private StackPane loadingOverlay;
    @FXML private VBox notificationArea;

    // Services
    private final ProduitService produitService = ProduitService.getInstance();
    private final CategorieService categorieService = new CategorieService();

    // Data structures
    private final ObservableList<Produit> allProducts = FXCollections.observableArrayList();
    private FilteredList<Produit> filteredProducts;
    private final Map<Integer, Category> categoriesMap = new HashMap<>();

    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger les catégories et créer un mapping
        loadCategories();

        // Initialisation des ComboBox
        initializeFilters();

        // Configuration du tableau
        configureTable();

        // Configuration de la pagination
        configurePagination();

        // Configuration des boutons
        setupEventHandlers();

        // Charger les données
        loadProducts();

        // Initialisation des statistiques
        updateStatistics();

        // Masquer l'overlay de chargement
        loadingOverlay.setVisible(false);
        Platform.runLater(() -> {
            if (searchField.getScene() != null) {
                String css = Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm();
                searchField.getScene().getStylesheets().add(css);
            }
        });
    }

    private void loadCategories() {
        List<Category> categories = categorieService.getAll();
        categoriesMap.clear();
        for (Category category : categories) {
            categoriesMap.put(category.getId(), category);
        }
    }

    private void initializeFilters() {
        // Initialiser le filtre de catégories
        categoryFilter.getItems().add("Toutes les catégories");
        List<Category> categories = categorieService.getAll();
        categoryFilter.getItems().addAll(categories.stream()
                .map(Category::getName)
                .toList());
        categoryFilter.setValue("Toutes les catégories");

        categoryFilter.setOnAction(event -> filterProducts());

        // Initialiser le filtre de statut
        statusFilter.getItems().addAll("Tous les statuts", "En stock", "Stock faible", "Rupture");
        statusFilter.setValue("Tous les statuts");
        statusFilter.setOnAction(event -> filterProducts());

        // Initialiser le ComboBox d'éléments par page
        itemsPerPageCombo.getItems().addAll("5", "10", "20", "50");
        itemsPerPageCombo.setValue("10");
        itemsPerPageCombo.setOnAction(event -> {
            itemsPerPage = Integer.parseInt(itemsPerPageCombo.getValue());
            currentPage = 1;
            updatePagination();
        });
    }

    private void configureTable() {
        // Configuration des colonnes du tableau
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Configurer la colonne de catégorie pour afficher le nom au lieu de l'ID
        categoryColumn.setCellValueFactory(cellData -> {
            int categoryId = cellData.getValue().getCategorie();
            Category category = categoriesMap.get(categoryId);
            String categoryName = category != null ? category.getName() : "N/A";
            return javafx.beans.binding.Bindings.createStringBinding(() -> categoryName);
        });

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        // Configurer la colonne de statut basée sur la quantité
        statusColumn.setCellValueFactory(cellData -> {
            int quantity = cellData.getValue().getQuantite();
            String status = quantity > 10 ? "En stock" : quantity > 0 ? "Stock faible" : "Rupture";
            return javafx.beans.binding.Bindings.createStringBinding(() -> status);
        });

        // Configurer la colonne d'actions avec des boutons
        addActionButtons();

        // Configuration de la sélection multiple
        productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> updateSelectedItemsLabel());
    }

    private void addActionButtons() {
        Callback<TableColumn<Produit, Void>, TableCell<Produit, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Produit, Void> call(final TableColumn<Produit, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Modifier");
                    private final Button deleteBtn = new Button("Supprimer");

                    {
                        editBtn.setOnAction(event -> {
                            Produit produit = getTableView().getItems().get(getIndex());
                            editProduct(produit);
                        });

                        deleteBtn.setOnAction(event -> {
                            Produit produit = getTableView().getItems().get(getIndex());
                            deleteProduct(produit);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
                            hbox.getChildren().addAll(editBtn, deleteBtn);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        };

        actionsColumn.setCellFactory(cellFactory);
    }

    private void configurePagination() {
        firstPageButton.setOnAction(event -> {
            currentPage = 1;
            updatePagination();
        });

        previousPageButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                updatePagination();
            }
        });

        nextPageButton.setOnAction(event -> {
            if (currentPage < totalPages) {
                currentPage++;
                updatePagination();
            }
        });

        lastPageButton.setOnAction(event -> {
            currentPage = totalPages;
            updatePagination();
        });
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(event -> refreshList());
        addProductButton.setOnAction(event -> navigateToAddProduct());
        deleteSelectedButton.setOnAction(event -> deleteSelectedProducts());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterProducts());
    }

    private void loadProducts() {
        loadingOverlay.setVisible(true);

        // Récupérer tous les produits depuis le service
        List<Produit> products = produitService.getAll();

        // Mettre à jour la liste observable
        allProducts.clear();
        allProducts.addAll(products);

        // Configurer le filtrage
        filteredProducts = new FilteredList<>(allProducts, p -> true);
        SortedList<Produit> sortedProducts = new SortedList<>(filteredProducts);
        sortedProducts.comparatorProperty().bind(productTable.comparatorProperty());

        // Mettre à jour la pagination
        updatePagination();

        // Mettre à jour les statistiques
        updateStatistics();

        loadingOverlay.setVisible(false);
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
        if (currentPage > totalPages && totalPages > 0) {
            currentPage = totalPages;
        }

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredProducts.size());

        ObservableList<Produit> pageItems = FXCollections.observableArrayList(
                filteredProducts.subList(startIndex, endIndex));

        productTable.setItems(pageItems);

        pageInfoLabel.setText(String.format("Page %d/%d", currentPage, totalPages));

        // Activer/désactiver les boutons de pagination
        firstPageButton.setDisable(currentPage == 1);
        previousPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(currentPage >= totalPages);
        lastPageButton.setDisable(currentPage >= totalPages);
    }

    private void updateStatistics() {
        int total = allProducts.size();
        int lowStock = 0;
        int outOfStock = 0;
        double totalValue = 0;

        for (Produit p : allProducts) {
            if (p.getQuantite() == 0) {
                outOfStock++;
            } else if (p.getQuantite() <= 10) {
                lowStock++;
            }
            totalValue += p.getPrix() * p.getQuantite();
        }

        totalProductsLabel.setText(String.valueOf(total));
        lowStockLabel.setText(String.valueOf(lowStock));
        outOfStockLabel.setText(String.valueOf(outOfStock));
        totalValueLabel.setText(String.format("%.2f€", totalValue));
    }

    private void refreshList() {
        loadProducts();
        showNotification("Liste des produits rafraîchie avec succès");
    }

    private void navigateToAddProduct() {
        try {
            // Charger le fichier FXML de la page d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutproduit.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène avec la page d'ajout
            Scene scene = new Scene(root);

            // Obtenir la fenêtre actuelle à partir du bouton
            Stage stage = (Stage) addProductButton.getScene().getWindow();

            // Définir la nouvelle scène sur la fenêtre existante
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la page d'ajout de produit: " + e.getMessage());
        }
    }

    private void filterProducts() {
        String searchText = searchField.getText().toLowerCase();
        String categoryName = categoryFilter.getValue();
        String status = statusFilter.getValue();

        filteredProducts.setPredicate(produit -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    produit.getNom().toLowerCase().contains(searchText) ||
                    produit.getDescription().toLowerCase().contains(searchText);

            boolean matchesCategory = "Toutes les catégories".equals(categoryName) ||
                    (categoriesMap.get(produit.getCategorie()) != null &&
                            categoriesMap.get(produit.getCategorie()).getName().equals(categoryName));

            boolean matchesStatus = switch (status) {
                case "Tous les statuts" -> true;
                case "En stock" -> produit.getQuantite() > 10;
                case "Stock faible" -> produit.getQuantite() > 0 && produit.getQuantite() <= 10;
                case null, default ->  // "Rupture"
                        produit.getQuantite() == 0;
            };

            return matchesSearch && matchesCategory && matchesStatus;
        });

        updatePagination();
    }

    private void editProduct(Produit produit) {
        try {
            // Charger le fichier FXML de la page d'édition
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et lui passer le produit à éditer
            EditProduct editController = loader.getController();
            editController.loadProductData(produit.getId());

            // Créer une nouvelle scène avec la page d'édition
            Scene scene = new Scene(root);

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) productTable.getScene().getWindow();

            // Définir la nouvelle scène sur la fenêtre existante
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la page d'édition: " + e.getMessage());
            showNotification("Erreur lors du chargement de la page d'édition");
        }
    }

    private void deleteProduct(Produit produit) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Suppression du produit");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le produit: " + produit.getNom() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                produitService.deleteProduit(produit.getId());
                allProducts.remove(produit);
                filterProducts();
                updateStatistics();
                showNotification("Produit supprimé avec succès");
            }
        });
    }

    private void deleteSelectedProducts() {
        List<Produit> selectedProducts = productTable.getSelectionModel().getSelectedItems();

        if (selectedProducts.isEmpty()) {
            showNotification("Aucun produit sélectionné");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression multiple");
        alert.setHeaderText("Suppression de produits");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer les " + selectedProducts.size() + " produits sélectionnés ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                for (Produit produit : selectedProducts) {
                    produitService.deleteProduit(produit.getId());
                    allProducts.remove(produit);
                }
                filterProducts();
                updateStatistics();
                showNotification(selectedProducts.size() + " produits supprimés avec succès");
            }
        });
    }

    private void updateSelectedItemsLabel() {
        int count = productTable.getSelectionModel().getSelectedItems().size();
        selectedItemsLabel.setText(count + " produit(s) sélectionné(s)");
    }

    private void showNotification(String message) {
        Label notification = new Label(message);
        notification.getStyleClass().add("notification");
        notificationArea.getChildren().add(notification);

        // Créer une transition pour faire disparaître la notification après quelques secondes
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        delay.setOnFinished(event -> notificationArea.getChildren().remove(notification));
        delay.play();
    }
}