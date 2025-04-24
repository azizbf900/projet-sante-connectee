package Services;

import models.Commentaire;
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

    // Ajouter un post
    public void ajouterPost(Posts post) {
        String sql = "INSERT INTO post (titre, contenu, date_publication, legende, `like`, dislike) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, post.getTitre());
            pst.setString(2, post.getContenu());
            pst.setDate(3, Date.valueOf(post.getDatePublication()));
            pst.setString(4, post.getLegende());
            pst.setInt(5, post.getLike());
            pst.setInt(6, post.getDislike());

            pst.executeUpdate();
            System.out.println("✅ Post ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du post : " + e.getMessage());
        }
    }

    // Récupérer tous les posts
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
                        rs.getString("legende"),
                        rs.getInt("likes"),
                        rs.getInt("dislikes")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des posts : " + e.getMessage());
        }

        return list;
    }

    // Modifier un post
    public void modifierPost(Posts post) {
        String sql = "UPDATE post SET titre = ?, contenu = ?, date_publication = ?, legende = ?, `like` = ?, dislike = ? WHERE id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, post.getTitre());
            pst.setString(2, post.getContenu());
            pst.setDate(3, Date.valueOf(post.getDatePublication()));
            pst.setString(4, post.getLegende());
            pst.setInt(5, post.getLike());
            pst.setInt(6, post.getDislike());
            pst.setInt(7, post.getId());

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

    // Supprimer un post
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

    // Incrémenter un like
    public void incrementerLike(int postId) {
        String sql = "UPDATE post SET `like` = `like` + 1 WHERE id = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, postId);
            pst.executeUpdate();
            System.out.println("👍 Like ajouté !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'incrémentation du like : " + e.getMessage());
        }
    }

    // Incrémenter un dislike
    public void incrementerDislike(int postId) {
        String sql = "UPDATE post SET dislike = dislike + 1 WHERE id = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, postId);
            pst.executeUpdate();
            System.out.println("👎 Dislike ajouté !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'incrémentation du dislike : " + e.getMessage());
        }
    }

    public void updateLikesDislikes(Posts post) {
        String sql = "UPDATE post SET likes = ?, dislikes = ? WHERE id = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, post.getLike());
            pst.setInt(2, post.getDislike());
            pst.setInt(3, post.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour des likes/dislikes : " + e.getMessage());
        }
    }

    public List<Posts> getAllPostsWithCommentaires() {
        List<Posts> posts = getAllPosts(); // ta méthode existante
        ServiceCommentaire sc = new ServiceCommentaire(); // ou injecte-le

        for (Posts post : posts) {
            List<Commentaire> commentaires = sc.getCommentairesByPostId(post.getId());
            post.setCommentaires(commentaires);
        }

        return posts;
    }

}

