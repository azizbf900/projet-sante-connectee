package models;

public class Category {

    int id;
    String name;
    String image_path;

    public Category() {}

    public Category(int id, String name, String image_path) {
        this.id = id;
        this.name = name;
        this.image_path = image_path;
    }

    public Category(String name, String image_path) {
        this.name = name;
        this.image_path = image_path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { // ✅ fixé ici
        this.name = name;
    }

    public String getimage_path() {
        return image_path;
    }

    public void setimage_path(String image_path) {
        this.image_path = image_path;
    }

    @Override
    public String toString() {
        return name; // ✅ utile pour combobox
    }
}