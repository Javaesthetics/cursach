package com.example.cursach.controllers;

import com.example.cursach.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HelloController {

    @FXML private Button log_in;
    @FXML private Button registration_button;
    @FXML private TextField login;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button togglePasswordButton;

    private static final String USERS_FILE = "users.json";

    @FXML
    void initialize() {
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);

        log_in.setOnAction(event -> loginUser());

        registration_button.setOnAction(event ->
                openNewWindow("/com/example/cursach/registration-view.fxml", registration_button)
        );

        togglePasswordButton.setOnAction(event -> togglePasswordVisibility());
    }

    private void loginUser() {
        String userLogin = login.getText().trim();
        String userPass = passwordField.isVisible()
                ? passwordField.getText()
                : passwordVisible.getText();

        if (userLogin.isEmpty() || userPass.isEmpty()) {
            showAlert("Помилка", "Введіть логін і пароль!");
            return;
        }

        if (userLogin.equals("admin") && userPass.equals("admin")) {
            openAdminPanel("admin");
            return;
        }

        List<User> users = loadUsers();

        Optional<User> foundUser = users.stream()
                .filter(u -> u != null
                        && u.getUsername() != null && u.getUsername().equals(userLogin)
                        && u.getPassword() != null && u.getPassword().equals(userPass))
                .findFirst();

        if (foundUser.isPresent()) {
            showAlert("Успіх", "Вхід виконано успішно!");
            openUserMenu(userLogin);
        } else {
            showAlert("Помилка входу", "Невірний логін або пароль.");
        }
    }

    private void openUserMenu(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/napriam-view.fxml"));
            Parent root = loader.load();

            try {
                NapriamController controller = loader.getController();
                controller.setUsername(username);
            } catch (Exception e) {
                System.out.println("Помилка передачі username в NapriamController");
            }

            Stage stage = new Stage();
            stage.setTitle("Вибір зали");
            stage.setScene(new Scene(root));
            stage.show();

            ((Stage) log_in.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Помилка", "Не вдалося відкрити вікно вибору зал!");
        }
    }

    private void openAdminPanel(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/artist.fxml"));
            Parent root = loader.load();

            ArtistController controller = loader.getController();
            controller.setUserSession(username, "admin");

            Stage stage = new Stage();
            stage.setTitle("Панель Адміністратора");
            stage.setScene(new Scene(root));
            stage.show();

            ((Stage) log_in.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Помилка", "Не вдалося завантажити панель адміністратора!");
        }
    }

    private List<User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<User>>() {}.getType();
            List<User> users = new Gson().fromJson(reader, listType);
            return users != null ? users : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void togglePasswordVisibility() {
        boolean isVisible = passwordVisible.isVisible();
        passwordVisible.setVisible(!isVisible);
        passwordVisible.setManaged(!isVisible);
        passwordField.setVisible(isVisible);
        passwordField.setManaged(isVisible);
    }

    private void openNewWindow(String fxmlFile, Button currentButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            ((Stage) currentButton.getScene().getWindow()).close();
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