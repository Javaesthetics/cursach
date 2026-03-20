package com.example.cursach.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Review {
    private String paintingTitle;
    private String username;
    private String text;
    private String date; // Тут тепер буде і дата, і час

    public Review(String paintingTitle, String username, String text) {
        this.paintingTitle = paintingTitle;
        this.username = username;
        this.text = text;

        // 🔥 Генеруємо дату та час у форматі: "22.12.2025 14:30"
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        this.date = now.format(formatter);
    }

    public String getPaintingTitle() { return paintingTitle; }
    public String getUsername() { return username; }
    public String getText() { return text; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        // Формат виводу для списку
        return "[" + date + "] " + username + ": " + text;
    }
}