package models;

public class Produit {

    int id;
    String nom;
    String description;
    double prix;
    int quantite;
    int categorie_id;
    String image_path;
    String model3D;

    public Produit() {}

    public Produit(int id, String nom, String description, double prix, int quantite, int categorie_id, String image_path, String model3D) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantite = quantite;
        this.categorie_id = categorie_id;
        this.image_path = image_path;
        this.model3D = model3D;
    }

    public Produit(String nom, String description, double prix, int quantite, int categorie_id, String image_path, String model3D) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantite = quantite;
        this.categorie_id = categorie_id;
        this.image_path = image_path;
        this.model3D = model3D;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public int getCategorie() {
        return categorie_id;
    }

    public void setCategorie(int categorieId) {
        this.categorie_id = categorieId;
    }

    public String getimage_path() {
        return image_path;
    }

    public void setimage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getModel3D() {
        return model3D;
    }

    public void setModel3D(String model3D) {
        this.model3D = model3D;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", quantite=" + quantite +
                ", categorieId=" + categorie_id +
                ", image_path='" + image_path + '\'' +
                ", model3D='" + model3D + '\'' +
                '}';
    }
}