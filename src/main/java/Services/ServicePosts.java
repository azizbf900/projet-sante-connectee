package Services;

import models.Posts;
import tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePosts {
    private Connection con;

    public ServicePosts() {
        con = MyDataBase.getInstance().getCnx();
    }

    public void ajouterPost(Posts post) {
        String sql = "INSERT INTO post (titre, contenu, date_publication, legende) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, post.getTitre());
            pst.setString(2, post.getContenu());
            pst.setDate(3, Date.valueOf(post.getDatePublication()));
            pst.setString(4, post.getLegende());

            pst.executeUpdate();
            System.out.println("✅ Post ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du post : " + e.getMessage());
        }
    }

    public List<Posts> getAllPosts() {
        List<Posts> list = new ArrayList<>();
        String sql = "SELECT * FROM post";

        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Posts p = new Posts(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getDate("date_publication").toLocalDate(),
                        rs.getString("legende")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des posts : " + e.getMessage());
        }

        return list;
    }

    public void modifierPost(Posts post) {
        String sql = "UPDATE post SET titre = ?, contenu = ?, date_publication = ?, legende = ? WHERE id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, post.getTitre());
            pst.setString(2, post.getContenu());
            pst.setDate(3, Date.valueOf(post.getDatePublication()));
            pst.setString(4, post.getLegende());
            pst.setInt(5, post.getId());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Post modifié avec succès !");
            } else {
                System.out.println("⚠️ Aucun post trouvé avec l'ID donné.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification du post : " + e.getMessage());
        }
    }

    public void supprimerPost(int id) {
        String sql = "DELETE FROM post WHERE id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("🗑️ Post supprimé avec succès !");
            } else {
                System.out.println("⚠️ Aucun post trouvé avec l'ID donné.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du post : " + e.getMessage());
        }
    }
}
