package models;

import java.time.LocalDate;

public class Commentaire {
    private int id;
    private int postId;
    private String contenu;
    private LocalDate dateCommentaire;

    // Constructeurs
    public Commentaire(int id, int postId, String contenu, LocalDate dateCommentaire) {
        this.id = id;
        this.postId = postId;
        this.contenu = contenu;
        this.dateCommentaire = dateCommentaire;
    }

    public Commentaire(int postId, String contenu, LocalDate dateCommentaire) {
        this.postId = postId;
        this.contenu = contenu;
        this.dateCommentaire = dateCommentaire;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDate getDateCommentaire() {
        return dateCommentaire;
    }

    public void setDateCommentaire(LocalDate dateCommentaire) {
        this.dateCommentaire = dateCommentaire;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", postId=" + postId +
                ", contenu='" + contenu + '\'' +
                ", dateCommentaire=" + dateCommentaire +
                '}';
    }
}
