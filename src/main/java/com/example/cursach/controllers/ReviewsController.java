package com.example.cursach.controllers;

import com.example.cursach.models.Review;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReviewsController {

    @FXML private Text artistNameText;
    @FXML private ListView<String> reviewsList;
    @FXML private TextArea reviewInput;
    @FXML private Button addButton;
    @FXML private Button closeButton;
    @FXML private Button deleteButton;

    private ArtistController.Painting currentPainting;
    private String currentUsername = "Гість";
    private String currentRole = "user";
    private final String FILE_PATH = "reviews.json";
    private ObservableList<String> reviewsDisplayList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        reviewsList.setItems(reviewsDisplayList);

        if (deleteButton != null) {
            deleteButton.setDisable(true);
            deleteButton.setVisible(false);
        }

        reviewsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (deleteButton != null && "admin".equalsIgnoreCase(currentRole)) {
                deleteButton.setDisable(newVal == null);
            }
        });
    }

    public void initData(ArtistController.Painting p, ArtistController.Artist a, String role, String username) {
        this.currentPainting = p;
        this.currentRole = (role != null) ? role : "user";
        this.currentUsername = (username != null) ? username : "Гість";

        String targetName = (p != null) ? p.getTitle() : "Картина";
        if (artistNameText != null) {
            artistNameText.setText("Відгуки: " + targetName);
        }

        loadAndFilterReviews(targetName);
        setupRoleBasedUI();
    }

    private void setupRoleBasedUI() {
        boolean isAdmin = "admin".equalsIgnoreCase(currentRole);

        if (reviewInput != null) reviewInput.setVisible(!isAdmin);
        if (addButton != null) addButton.setVisible(!isAdmin);

        if (deleteButton != null) {
            deleteButton.setVisible(isAdmin);
            deleteButton.setOnAction(e -> handleAdminDelete());
        }

        if (isAdmin) {
            reviewsList.setPrefHeight(300);
        } else {
            if (addButton != null) addButton.setOnAction(e -> addReview());
        }

        if (closeButton != null) {
            closeButton.setOnAction(e -> ((Stage) closeButton.getScene().getWindow()).close());
        }
    }

    private void handleAdminDelete() {
        String selectedReview = reviewsList.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            deleteReviewFromFile(selectedReview);
            reviewsDisplayList.remove(selectedReview);
            deleteButton.setDisable(true);
        }
    }

    private void addReview() {
        String text = reviewInput.getText().trim();
        if (text.isEmpty()) return;

        Review newReview = new Review(currentPainting.getTitle(), currentUsername, text);
        reviewsDisplayList.add(newReview.toString());
        saveReviewToFile(newReview);
        reviewInput.clear();
        reviewsList.scrollTo(reviewsDisplayList.size() - 1);
    }

    private void loadAndFilterReviews(String targetTitle) {
        reviewsDisplayList.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Review>>(){}.getType();
            List<Review> allReviews = new Gson().fromJson(reader, listType);

            if (allReviews != null) {
                for (Review r : allReviews) {
                    if (targetTitle.equals(r.getPaintingTitle())) {
                        reviewsDisplayList.add(r.toString());
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveReviewToFile(Review newReview) {
        List<Review> allReviews = loadFullList();
        allReviews.add(newReview);
        writeListToFile(allReviews);
    }

    private void deleteReviewFromFile(String reviewString) {
        List<Review> allReviews = loadFullList();
        allReviews.removeIf(r -> r.toString().equals(reviewString));
        writeListToFile(allReviews);
    }

    private List<Review> loadFullList() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Review>>(){}.getType();
            List<Review> list = new Gson().fromJson(reader, listType);
            return (list != null) ? list : new ArrayList<>();
        } catch (IOException e) { return new ArrayList<>(); }
    }

    private void writeListToFile(List<Review> list) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(list, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }
}