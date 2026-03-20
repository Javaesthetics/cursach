package com.example.cursach.controllers;

import com.example.cursach.controllers.ArtistController.Artist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NapriamController {

    @FXML private Button impr;
    @FXML private Button modern;
    @FXML private Button postimpr;
    @FXML private Button backButton;
    @FXML private Button profileButton;

    @FXML private ListView<String> imprSp;
    @FXML private ListView<String> modernsSp;
    @FXML private ListView<String> postimprSp;

    private final File jsonFile = new File("artists.json");
    private String currentUsername = "Гість";

    public void setUsername(String username) {
        if (username != null && !username.isEmpty()) {
            this.currentUsername = username;
        }
    }

    public void setDisplayName(String username) {
        this.currentUsername = username;
    }

    @FXML
    public void initialize() {
        loadAndSortArtists();

        if (backButton != null) {
            backButton.setOnAction(event -> goBack());
        }

        if (profileButton != null) {
            profileButton.setOnAction(event -> openProfile());
        }
    }

    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/Profile.fxml"));
            Parent root = loader.load();
            ProfileController pc = loader.getController();
            pc.initData(currentUsername, "user");

            Stage stage = new Stage();
            stage.setTitle("Мій профіль");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Помилка", "Не вдалося відкрити профіль!");
        }
    }

    private void openZalaArtist(String directionName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/zalaArtist.fxml"));
            Parent root = loader.load();

            ZalaArtistController controller = loader.getController();
            controller.setInfo(directionName, currentUsername);

            Stage stage = (Stage) impr.getScene().getWindow();
            stage.setTitle("Зала: " + directionName);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Помилка", "Не вдалося відкрити залу!");
        }
    }

    @FXML private void handleImprButton(ActionEvent event) { openZalaArtist("Імпресіонізм"); }
    @FXML private void handleModernButton(ActionEvent event) { openZalaArtist("Модернізм"); }
    @FXML private void handlePostimprButton(ActionEvent event) { openZalaArtist("Постімпресіонізм"); }

    @FXML
    private void handleInfoImpr(ActionEvent event) {
        showAlert("Імпресіонізм", "Мистецький напрям, заснований на принципі безпосередньої фіксації вражень.");
    }

    @FXML
    private void handleInfoModern(ActionEvent event) {
        showAlert("Модерн", "Стиль у мистецтві, що характеризується відмовою від прямих ліній.");
    }

    @FXML
    private void handleInfoPostImpr(ActionEvent event) {
        showAlert("Постімпресіонізм", "Напрям, який виник як реакція на імпресіонізм.");
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/hello-view.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setTitle("Головне меню");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAndSortArtists() {
        if (imprSp == null || modernsSp == null || postimprSp == null) return;

        imprSp.getItems().clear();
        modernsSp.getItems().clear();
        postimprSp.getItems().clear();

        List<Artist> allArtists = loadArtistsFromJson();
        for (Artist artist : allArtists) {
            if (artist.getName() == null || artist.getDirection() == null) continue;
            String dir = artist.getDirection().trim().toLowerCase();

            if (dir.contains("імпресіонізм") && !dir.contains("пост")) imprSp.getItems().add(artist.getName());
            else if (dir.contains("модерн") || dir.contains("модернізм")) modernsSp.getItems().add(artist.getName());
            else if (dir.contains("постімпресіонізм")) postimprSp.getItems().add(artist.getName());
        }
    }

    private List<Artist> loadArtistsFromJson() {
        if (!jsonFile.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(jsonFile)) {
            Type listType = new TypeToken<ArrayList<Artist>>() {}.getType();
            List<Artist> loaded = new Gson().fromJson(reader, listType);
            return (loaded != null) ? loaded : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void showAlert(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/CustomAlert.fxml"));
            Parent root = loader.load();

            CustomAlertController controller = loader.getController();
            controller.setInfo(title, message);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Помилка завантаження кастомного вікна!");
        }
    }
}