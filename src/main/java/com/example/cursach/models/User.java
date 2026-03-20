package com.example.cursach.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String email;
    private String avatarPath; // 🔥 Назва поля у вашій моделі
    private String role;
    private List<String> favorites = new ArrayList<>();

    // Порожній конструктор для GSON
    public User() {}

    public User(String username, String password, String email, String avatarPath, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatarPath = avatarPath;
        this.role = role;
    }

    // Геттери та Сеттери
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getFavorites() { return favorites; }
    public void setFavorites(List<String> favorites) { this.favorites = favorites; }

    public void addFavorite(String paintingTitle) {
        if (favorites == null) favorites = new ArrayList<>();
        if (!favorites.contains(paintingTitle)) {
            favorites.add(paintingTitle);
        }
    }
}