package com.example.cursach.controllers;

import com.example.cursach.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegistrationController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private ImageView avatarImage;
    @FXML private Button chooseAvatarButton;
    @FXML private Button registerButton;

    private static final String USERS_FILE = "users.json";
    private String selectedAvatarPath = "";

    @FXML
    void initialize() {
        registerButton.setOnAction(event -> registerUser());

        if (chooseAvatarButton != null) {
            chooseAvatarButton.setOnAction(event -> chooseAvatar());
        }
    }

    private void chooseAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Оберіть фото профілю");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) chooseAvatarButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String savedRelativePath = saveImageToProject(file);

            if (savedRelativePath != null) {
                this.selectedAvatarPath = savedRelativePath;
                File newFile = new File(savedRelativePath);
                avatarImage.setImage(new Image(newFile.toURI().toString()));
            }
        }
    }

    private String saveImageToProject(File sourceFile) {
        try {
            File destDir = new File("images");
            if (!destDir.exists()) destDir.mkdirs();

            String extension = "";
            int i = sourceFile.getName().lastIndexOf('.');
            if (i > 0) extension = sourceFile.getName().substring(i);

            String newFileName = UUID.randomUUID().toString() + extension;
            File destFile = new File(destDir, newFileName);

            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return "images/" + newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Помилка", "Не вдалося зберегти аватар у папку проєкту.");
            return null;
        }
    }

    private void registerUser() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();

        if (login.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert("Помилка", "Будь ласка, заповніть усі поля!");
            return;
        }

        List<User> users = loadUsers();

        for (User u : users) {
            if (u != null && u.getUsername() != null && u.getUsername().equalsIgnoreCase(login)) {
                showAlert("Помилка", "Такий логін уже існує!");
                return;
            }
        }

        User newUser = new User(login, password, email, selectedAvatarPath, "user");

        users.add(newUser);
        saveUsers(users);

        showAlert("Успіх", "Реєстрація пройшла успішно!");
        openLoginWindow();

        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.close();
    }

    private List<User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<User>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveUsers(List<User> users) {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/hello-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Авторизація");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}