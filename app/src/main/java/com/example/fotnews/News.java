package com.example.fotnews;

public class News {
    private String category;
    private String description;
    private String imageUrl;
    private String title;

    // Default constructor (required for Firebase)
    public News() {
    }

    // Constructor with all parameters
    public News(String category, String description, String imageUrl, String title) {
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.title = title;
    }

    // Getters
    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    // Setters
    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}