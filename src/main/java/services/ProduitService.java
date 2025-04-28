package services;

import models.Produit;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService {
    private static ProduitService instance;
    private final Connection con;

    public ProduitService() {
        con = MyDatabase.getCon();
    }

    public static ProduitService getInstance() {
        if (instance == null) {
            instance = new ProduitService();
        }
        return instance;
    }

    // Statistiques avancées
    public double getAveragePriceByCategory(int categoryId) {
        String query = "SELECT AVG(prix) as avg_price FROM produit WHERE categorie_id = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, categoryId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_price");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du prix moyen: " + e.getMessage());
        }
        return 0.0;
    }

    public int getTotalStockByCategory(int categoryId) {
        String query = "SELECT SUM(quantite) as total_stock FROM produit WHERE categorie_id = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, categoryId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_stock");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du stock total: " + e.getMessage());
        }
        return 0;
    }

    public int getProductsCountByCategory(int categoryId) {
        String query = "SELECT COUNT(*) as count FROM produit WHERE categorie_id = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, categoryId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des produits: " + e.getMessage());
        }
        return 0;
    }

    public String getMostSoldCategory() {
        String query = "SELECT c.name, COALESCE(SUM(p.quantite), 0) as total " +
                "FROM categorie c " +
                "LEFT JOIN produit p ON c.id = p.categorie_id " +
                "GROUP BY c.id, c.name " +
                "ORDER BY total DESC LIMIT 1";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    if (total > 0) {
                        return rs.getString("name") + " (" + total + " unités)";
                    }
                }
                return "Aucune vente enregistrée";
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            return "Erreur de calcul";
        }
    }

    // Recherche avancée
    public List<Produit> searchProducts(String keyword, Double minPrice, Double maxPrice, Integer categoryId) {
        List<Produit> produits = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM produit WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (nom LIKE ? OR description LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (minPrice != null) {
            sql.append(" AND prix >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND prix <= ?");
            params.add(maxPrice);
        }
        if (categoryId != null) {
            sql.append(" AND categorie_id = ?");
            params.add(categoryId);
        }

        try (PreparedStatement pstmt = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Produit produit = new Produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite"),
                        rs.getInt("categorie_id"),
                        rs.getString("image_path"),
                        rs.getString("model3D")
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche avancée: " + e.getMessage());
        }
        return produits;
    }

    // Gestion du stock
    public boolean updateStock(int productId, int quantityChange) {
        String sql = "UPDATE produit SET quantite = quantite + ? WHERE id = ? AND quantite + ? >= 0";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, quantityChange);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantityChange);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du stock: " + e.getMessage());
            return false;
        }
    }

    // Ajouter un produit
    public void ajouterProduit(Produit produit) {
        String sql = "INSERT INTO produit (nom, description, prix, quantite, categorie_id, image_path, model3D) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, produit.getNom());
            pstmt.setString(2, produit.getDescription());
            pstmt.setDouble(3, produit.getPrix());
            pstmt.setInt(4, produit.getQuantite());
            pstmt.setInt(5, produit.getCategorie());
            pstmt.setString(6, produit.getimage_path());
            pstmt.setString(7, produit.getModel3D());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du produit: " + e.getMessage());
        }
    }

    // Obtenir tous les produits
    public List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Produit produit = new Produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite"),
                        rs.getInt("categorie_id"),
                        rs.getString("image_path"),
                        rs.getString("model3D")
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits: " + e.getMessage());
        }
        return produits;
    }

    // Récupère un produit par son ID
    public Produit getById(int id) {
        String sql = "SELECT * FROM produit WHERE id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite"),
                        rs.getInt("categorie_id"),
                        rs.getString("image_path"),
                        rs.getString("model3D")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du produit par ID: " + e.getMessage());
        }
        return null;
    }

    // Mise à jour d'un produit
    public boolean updateProduit(Produit produit) {
        String sql = "UPDATE produit SET nom = ?, description = ?, prix = ?, quantite = ?, categorie_id = ?, image_path = ?, model3D = ? WHERE id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, produit.getNom());
            pstmt.setString(2, produit.getDescription());
            pstmt.setDouble(3, produit.getPrix());
            pstmt.setInt(4, produit.getQuantite());
            pstmt.setInt(5, produit.getCategorie());
            pstmt.setString(6, produit.getimage_path());
            pstmt.setString(7, produit.getModel3D());
            pstmt.setInt(8, produit.getId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du produit: " + e.getMessage());
            return false;
        }
    }

    // Suppression d'un produit par son ID
    public void deleteProduit(int id) {
        String sql = "DELETE FROM produit WHERE id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du produit: " + e.getMessage());
        }
    }

    // Produits par catégorie
    public List<Produit> getByCategorie(int categorieId) {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit WHERE categorie_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, categorieId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Produit produit = new Produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite"),
                        rs.getInt("categorie_id"),
                        rs.getString("image_path"),
                        rs.getString("model3D")
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des produits par catégorie: " + e.getMessage());
        }
        return produits;
    }
}