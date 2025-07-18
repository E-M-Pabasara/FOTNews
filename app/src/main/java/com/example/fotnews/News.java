package com.example.fotnews;

public class News {
    private String category;
    private String description;
    private String imageUrl;
    private String title;


    public News() {
    }


    public News(String category, String description, String imageUrl, String title) {
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.title = title;
    }


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