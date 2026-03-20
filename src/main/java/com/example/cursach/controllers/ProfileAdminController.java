package com.example.cursach.controllers;

import com.example.cursach.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ProfileAdminController {

    @FXML private Text loginText;
    @FXML private Text emailText;
    @FXML private ImageView avatarImage;
    @FXML private Button closeButton;
    @FXML private Button changeAvatarButton;

    private String currentUsername;
    private final double AVATAR_SIZE = 120.0;
    private final String USERS_FILE = "users.json";

    @FXML
    public void initialize() {
        if (closeButton != null) {
            closeButton.setOnAction(e -> ((Stage) closeButton.getScene().getWindow()).close());
        }

        if (changeAvatarButton != null) {
            changeAvatarButton.setOnAction(e -> handleChangeAvatar());
        }
    }

    public void initData(String username) {
        this.currentUsername = username;
        loginText.setText(username);
        loadAdminProfileData(username);
    }

    private void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Оберіть новий аватар");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(avatarImage.getScene().getWindow());

        if (selectedFile != null) {
            try {
                File imagesDir = new File("images");
                if (!imagesDir.exists()) imagesDir.mkdir();

                String fileName = "avatar_" + currentUsername + "_" + System.currentTimeMillis() + ".jpg";
                File destFile = new File(imagesDir, fileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                Image newImg = new Image(destFile.toURI().toString());
                applySmartCrop(avatarImage, newImg);

                updateAvatarPathInJson(destFile.getPath());

                new Alert(Alert.AlertType.INFORMATION, "Аватар успішно оновлено та збережено!").show();

            } catch (IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Помилка при збереженні файлу.").show();
            }
        }
    }

    private void updateAvatarPathInJson(String newPath) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<User> users;

            try (FileReader reader = new FileReader(USERS_FILE)) {
                Type listType = new TypeToken<ArrayList<User>>(){}.getType();
                users = gson.fromJson(reader, listType);
            }

            if (users == null) users = new ArrayList<>();

            for (User u : users) {
                if (u.getUsername().equals(currentUsername)) {
                    u.setAvatarPath(newPath);
                    break;
                }
            }

            try (FileWriter writer = new FileWriter(USERS_FILE)) {
                gson.toJson(users, writer);
                writer.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAdminProfileData(String username) {
        File file = new File(USERS_FILE);

        try {
            var resource = getClass().getResource("images/avatar_admin_1766361830278.jpg");
            if (resource != null) {
                Image defaultImg = new Image(resource.toExternalForm());
                avatarImage.setImage(defaultImg);
                applySmartCrop(avatarImage, defaultImg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!file.exists()) return;
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
}