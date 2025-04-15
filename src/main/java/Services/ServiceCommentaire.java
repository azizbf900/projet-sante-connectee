package Services;

import models.Commentaire;
import tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire {

    private Connection con;

    public ServiceCommentaire() {
        con = MyDataBase.getInstance().getCnx();
    }

    // Ajouter un commentaire
    public void ajouter(Commentaire c) {
        String sql = "INSERT INTO commentaire (post_id, contenu, date_commentaire) VALUES (?, ?, ?)";
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, c.getPostId());
            pst.setString(2, c.getContenu());
            pst.setDate(3, Date.valueOf(c.getDateCommentaire()));
            pst.executeUpdate();
            System.out.println("✅ Commentaire ajouté !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    // Modifier un commentaire
    public void modifier(Commentaire c) {
        String sql = "UPDATE commentaire SET contenu = ?, date_commentaire = ? WHERE id = ?";
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, c.getContenu());
            pst.setDate(2, Date.valueOf(c.getDateCommentaire()));
            pst.setInt(3, c.getId());
            pst.executeUpdate();
            System.out.println("✅ Commentaire modifié !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    // Supprimer un commentaire
    public void supprimer(int id) {
        String sql = "DELETE FROM commentaire WHERE id = ?";
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("🗑️ Commentaire supprimé !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // Afficher tous les commentaires
    public List<Commentaire> afficher() {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Commentaire c = new Commentaire(
                        rs.getInt("id"),
                        rs.getInt("post_id"),
                        rs.getString("contenu"),
                        rs.getDate("date_commentaire").toLocalDate()
                );
                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage : " + e.getMessage());
        }
        return commentaires;
    }

    public List<Commentaire> getCommentairesByPostId(int postId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE post_id = ?";
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, postId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Commentaire c = new Commentaire(
                        rs.getInt("id"),
                        rs.getInt("post_id"),
                        rs.getString("contenu"),
                        rs.getDate("date_commentaire").toLocalDate()
                );
                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des commentaires du post : " + e.getMessage());
        }
        return commentaires;
    }
}
