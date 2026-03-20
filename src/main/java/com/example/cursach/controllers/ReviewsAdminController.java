package com.example.cursach.controllers;

import com.example.cursach.models.Review;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReviewsAdminController {

    @FXML private ListView<String> reviewsList;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;

    private ArtistController.Painting currentPainting;
    private final String FILE_PATH = "reviews.json";
    private ObservableList<String> reviewsDisplayList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        reviewsList.setItems(reviewsDisplayList);
        deleteButton.setDisable(true);

        reviewsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteButton.setDisable(newVal == null);
        });

        closeButton.setOnAction(e -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        deleteButton.setOnAction(e -> handleDeleteAction());
    }

    public void initData(ArtistController.Painting painting, String username) {
        this.currentPainting = painting;
        loadAndFilterReviews(painting.getTitle());
    }

    private void loadAndFilterReviews(String targetTitle) {
        reviewsDisplayList.clear();
        List<Review> allReviews = loadAllReviews();

        if (allReviews != null) {
            for (Review r : allReviews) {
                if (targetTitle.equals(r.getPaintingTitle())) {
                    reviewsDisplayList.add(r.toString());
                }
            }
        }
    }

    private void handleDeleteAction() {
        String selected = reviewsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження видалення");
        alert.setHeaderText("Ви впевнені, що хочете видалити цей відгук?");
        alert.setContentText("Цю дію неможливо буде скасувати.");
        alert.getDialogPane().setStyle("-fx-background-color: #FAF0E6; -fx-border-color: #d2b48c;");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            reviewsDisplayList.remove(selected);

            List<Review> allReviews = loadAllReviews();
            allReviews.removeIf(r -> r.toString().equals(selected));

            saveReviewsToFile(allReviews);
            reviewsList.getSelectionModel().clearSelection();
        }
    }

    private List<Review> loadAllReviews() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Review>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveReviewsToFile(List<Review> reviews) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(reviews, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}