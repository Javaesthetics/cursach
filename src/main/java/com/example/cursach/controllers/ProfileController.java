package com.example.cursach.controllers;

import com.example.cursach.models.User;
import com.example.cursach.controllers.DirectionGalleryController.FavoriteItem;
import com.example.cursach.controllers.ArtistController.Painting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.MouseButton;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileController {

    @FXML private Text loginText;
    @FXML private Text emailText;
    @FXML private ImageView avatarImage;
    @FXML private ListView<String> favoritesList;
    @FXML private Button closeButton;
    @FXML private Text favoritesLabel;
    @FXML private VBox favoritesContainer;

    private static final String USERS_FILE = "users.json";
    private static final String FAVORITES_FILE = "favorites.json";
    private static final String PAINTINGS_FILE = "paintings.json";
    private String currentUsername;
    private String currentRole;

    private final double AVATAR_SIZE = 120.0;

    @FXML
    public void initialize() {
        if (closeButton != null) {
            closeButton.setOnAction(e -> ((Stage) closeButton.getScene().getWindow()).close());
        }

        if (avatarImage != null) {
            avatarImage.setCursor(javafx.scene.Cursor.HAND);
            avatarImage.setOnMouseClicked(e -> changeAvatar());
            Tooltip.install(avatarImage, new Tooltip("ЛКМ: змінити аватар"));
        }

        setupFavoritesListInteractions();
    }

    public void initData(String username, String role) {
        this.currentUsername = username;
        this.currentRole = (role != null) ? role : "user";

        applyDataToUI(username);

        if ("admin".equalsIgnoreCase(currentRole)) {
            if (favoritesList != null) {
                favoritesList.setVisible(false);
                favoritesList.setManaged(false);
                favoritesList.setOpacity(0);
                favoritesList.setDisable(true);
            }

            if (favoritesLabel != null) {
                favoritesLabel.setVisible(false);
                favoritesLabel.setManaged(false);
                favoritesLabel.setOpacity(0);
            }

            if (closeButton != null) {
                closeButton.setLayoutY(285.0);
            }

            if (loginText != null) {
                loginText.setText(username + " (Адміністратор)");
            }
        } else {
            if (favoritesList != null) {
                favoritesList.setVisible(true);
                favoritesList.setManaged(true);
                favoritesList.setOpacity(1);
                favoritesList.setPrefHeight(130);
            }
            if (favoritesLabel != null) {
                favoritesLabel.setVisible(true);
                favoritesLabel.setManaged(true);
                favoritesLabel.setOpacity(1);
            }
            loadUserFavorites(username);
        }
    }

    private void setupFavoritesListInteractions() {
        if (favoritesList == null) return;

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Видалити з обраного");
        deleteItem.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        contextMenu.getItems().add(deleteItem);

        deleteItem.setOnAction(event -> {
            String selected = favoritesList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                confirmAndRemove(selected);
            }
        });

        favoritesList.setOnMouseClicked(event -> {
            String selectedTitle = favoritesList.getSelectionModel().getSelectedItem();
            if (selectedTitle == null) return;

            if (event.getButton() == MouseButton.PRIMARY) {
                contextMenu.hide();
                openPaintingFullView(selectedTitle);
            }
            else if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(favoritesList, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void confirmAndRemove(String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження");
        alert.setHeaderText("Видалити з обраного?");
        alert.setContentText("Ви впевнені щодо '" + title + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            removeFavorite(title);
        }
    }

    private void removeFavorite(String title) {
        File file = new File(FAVORITES_FILE);
        if (!file.exists()) return;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<FavoriteItem> favorites = new ArrayList<>();
        try {
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<FavoriteItem>>() {}.getType();
                List<FavoriteItem> loaded = gson.fromJson(reader, listType);
                if (loaded != null) favorites = loaded;
            }
            favorites.removeIf(f -> f.getUsername().equals(currentUsername) && f.getPaintingTitle().equals(title));
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(favorites, writer);
            }
            loadUserFavorites(currentUsername);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openPaintingFullView(String title) {
        File file = new File(PAINTINGS_FILE);
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Painting>>() {}.getType();
            List<Painting> allPaintings = new Gson().fromJson(reader, listType);
            if (allPaintings != null) {
                for (Painting p : allPaintings) {
                    if (p.getTitle().equals(title)) {
                        showImagePopup(p.getImagePath(), p.getTitle());
                        return;
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showImagePopup(String path, String title) {
        if (path == null || path.isEmpty()) return;
        File imgFile = new File(path);
        if (!imgFile.exists()) return;
        Stage stage = new Stage();
        stage.setTitle("Перегляд: " + title);
        ImageView view = new ImageView(new Image(imgFile.toURI().toString()));
        view.setPreserveRatio(true);
        view.setFitWidth(800);
        stage.setScene(new Scene(new BorderPane(new ScrollPane(view)), 850, 650));
        stage.show();
    }

    private void changeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Оберіть новий аватар");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(avatarImage.getScene().getWindow());
        if (selectedFile != null) {
            try {
                File imagesDir = new File("images");
                if (!imagesDir.exists()) imagesDir.mkdir();
                String fileName = "avatar_" + currentUsername + "_" + System.currentTimeMillis() + ".jpg";
                File destFile = new File(imagesDir, fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                applySmartCrop(avatarImage, new Image(destFile.toURI().toString()));
                updateUserAvatarPath(destFile.getPath());
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void updateUserAvatarPath(String newPath) {
        try (FileReader reader = new FileReader(USERS_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<User> users = gson.fromJson(reader, new TypeToken<ArrayList<User>>(){}.getType());
            if (users != null) {
                for (User u : users) {
                    if (u.getUsername().equals(currentUsername)) { u.setAvatarPath(newPath); break; }
                }
                try (FileWriter writer = new FileWriter(USERS_FILE)) { gson.toJson(users, writer); }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void applyDataToUI(String username) {
        if (loginText != null) loginText.setText(username);
        User user = findUserInDatabase(username);
        if (user != null) {
            if (emailText != null) emailText.setText(user.getEmail() != null ? user.getEmail() : "email@example.com");
            String path = user.getAvatarPath();
            if (path != null && !path.isEmpty()) {
                File f = new File(path);
                if (f.exists()) applySmartCrop(avatarImage, new Image(f.toURI().toString()));
            }
        }
    }

    private void applySmartCrop(ImageView imageView, Image image) {
        double scale = Math.max(AVATAR_SIZE / image.getWidth(), AVATAR_SIZE / image.getHeight());
        double cropW = AVATAR_SIZE / scale;
        double cropH = AVATAR_SIZE / scale;
        imageView.setViewport(new Rectangle2D((image.getWidth() - cropW) / 2, (image.getHeight() - cropH) / 2, cropW, cropH));
        imageView.setFitWidth(AVATAR_SIZE);
        imageView.setFitHeight(AVATAR_SIZE);
        imageView.setPreserveRatio(false);
        imageView.setImage(image);
    }

    private void loadUserFavorites(String username) {
        if (favoritesList == null) return;
        favoritesList.getItems().clear();
        File file = new File(FAVORITES_FILE);
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            List<FavoriteItem> all = new Gson().fromJson(reader, new TypeToken<ArrayList<FavoriteItem>>(){}.getType());
            if (all != null) {
                for (FavoriteItem f : all) {
                    if (f.getUsername().equals(username)) favoritesList.getItems().add(f.getPaintingTitle());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private User findUserInDatabase(String username) {
        try (FileReader reader = new FileReader(USERS_FILE)) {
            List<User> users = new Gson().fromJson(reader, new TypeToken<ArrayList<User>>(){}.getType());
            if (users != null) {
                for (User u : users) if (u.getUsername().equals(username)) return u;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}