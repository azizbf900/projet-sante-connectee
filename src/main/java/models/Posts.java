package models;

import java.time.LocalDate;
import java.util.List;

public class Posts {
    private int id;
    private String titre;
    private String contenu;
    private LocalDate datePublication;
    private String legende;
    private int likes;
    private int dislikes;

    // ✅ Liste de commentaires
    private List<Commentaire> commentaires;

    // Constructeur complet
    public Posts(int id, String titre, String contenu, LocalDate datePublication, String legende, int like, int dislike) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.legende = legende;
        this.likes = like;
        this.dislikes = dislike;
    }

    // Constructeur pour l'ajout sans ID ni like/dislike
    public Posts(String titre, String contenu, LocalDate datePublication, String legende) {
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.legende = legende;
        this.likes = 0;
        this.dislikes = 0;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDate getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDate datePublication) {
        this.datePublication = datePublication;
    }

    public String getLegende() {
        return legende;
    }

    public void setLegende(String legende) {
        this.legende = legende;
    }

    public int getLike() {
        return likes;
    }

    public void setLike(int like) {
        this.likes = like;
    }

    public int getDislike() {
        return dislikes;
    }

    public void setDislike(int dislike) {
        this.dislikes = dislike;
    }

    // ✅ Accesseurs pour les commentaires
    public List<Commentaire> getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(List<Commentaire> commentaires) {
        this.commentaires = commentaires;
    }

    // ✅ Pour l'image du post
    public String getImagePath() {
        return "file:src/images/" + contenu; // ou adapter selon le nom de l'image réelle
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", datePublication=" + datePublication +
                ", legende='" + legende + '\'' +
                ", like=" + likes +
                ", dislike=" + dislikes +
                ", nbCommentaires=" + (commentaires != null ? commentaires.size() : 0) +
                '}';
    }
}
