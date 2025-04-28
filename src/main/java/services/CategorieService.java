package services;

import interfaces.IService;
import models.Category;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<Category> {

    private final Connection con;

    public CategorieService() {
        con = MyDatabase.getCon();
    }

    @Override
    public boolean add(Category categorie) {
        String sql = "INSERT INTO categorie (name, image_path) VALUES (?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, categorie.getName());
            pstmt.setString(2, categorie.getimage_path());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la catégorie : " + e.getMessage());
            return false;
        }
    }

    @Override
    public void update(Category categorie) {
        String sql = "UPDATE categorie SET name = ?, image_path = ? WHERE id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, categorie.getName());
            pstmt.setString(2, categorie.getimage_path());
            pstmt.setInt(3, categorie.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de la catégorie : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM categorie WHERE id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la catégorie : " + e.getMessage());
        }
    }

    @Override
    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categorie";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("image_path")
                );
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des catégories : " + e.getMessage());
        }
        return categories;
    }

    public Category getById(int id) {
        String sql = "SELECT * FROM categorie WHERE id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("image_path")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de la catégorie par ID : " + e.getMessage());
        }
        return null;
    }

    // Fonctionnalité supplémentaire: Recherche d'une catégorie par nom
    public Category getByName(String name) {
        String sql = "SELECT * FROM categorie WHERE name = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("image_path")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de la catégorie par nom : " + e.getMessage());
        }
        return null;
    }
}