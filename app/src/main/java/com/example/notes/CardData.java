package com.example.notes;

public class CardData {
    private String title;
    private String description;

    public CardData(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
