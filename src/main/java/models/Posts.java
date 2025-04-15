package models;

import java.time.LocalDate;

public class Posts {
    private int id;
    private String titre;
    private String contenu;
    private LocalDate datePublication;
    private String legende;

    // Constructeurs
    public Posts(int id, String titre, String contenu, LocalDate datePublication, String legende) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.legende = legende;
    }

    public Posts(String titre, String contenu, LocalDate datePublication, String legende) {
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.legende = legende;
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

    public String getImagePath() {
        return "file:src/images/" + contenu; // ou le chemin relatif vers ton dossier images
    }


    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", datePublication=" + datePublication +
                ", legende='" + legende + '\'' +
                '}';
    }
}

