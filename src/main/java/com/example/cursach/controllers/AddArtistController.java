package com.example.cursach.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class AddArtistController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> directionBox;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView artistImageView;
    @FXML private Button saveButton;
    @FXML private Button chooseImageButton;
    @FXML private Button backButton;

    private ArtistController mainController;
    private ArtistController.Artist artistToEdit;
    private String selectedImagePath = "";

    @FXML
    void initialize() {

        directionBox.getItems().setAll("Імпресіонізм", "Постімпресіонізм", "Модернізм", "Модерн");

        // Прив'язка подій
        chooseImageButton.setOnAction(e -> chooseImage());
        saveButton.setOnAction(e -> saveArtist());

        if (backButton != null) {
            backButton.setOnAction(e -> closeWindow());
        }
    }

    public void setMainController(ArtistController mainController) {
        this.mainController = mainController;
    }

    public void setArtistToEdit(ArtistController.Artist artist) {
        this.artistToEdit = artist;
        if (artist != null) {
            nameField.setText(artist.getName());
            directionBox.setValue(artist.getDirection());
            descriptionArea.setText(artist.getDescription());
            selectedImagePath = artist.getImagePath();

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                File file = new File(selectedImagePath);
                if (file.exists()) {
                    artistImageView.setImage(new Image(file.toURI().toString()));
                }
            }
        }
    }

    private void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Оберіть фото художника");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) chooseImageButton.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);

        if (file != null) {
            String savedPath = saveImageToProject(file);
            if (savedPath != null) {
                this.selectedImagePath = savedPath;
                artistImageView.setImage(new Image(new File(savedPath).toURI().toString()));
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
            return null;
        }
    }

    private void saveArtist() {
        String name = nameField.getText().trim();
        String direction = directionBox.getValue();
        String description = descriptionArea.getText().trim();

        if (name.isEmpty() || direction == null) {
            showAlert("Помилка", "Будь ласка, введіть ім'я та оберіть напрям!");
            return;
        }

        if (mainController == null) {
            showAlert("Помилка", "Системна помилка: головний контролер не знайдено!");
            return;
        }

        if (artistToEdit == null) {
            ArtistController.Artist newArtist = new ArtistController.Artist(name, direction);
            newArtist.setDescription(description);
            newArtist.setImagePath(selectedImagePath);
            mainController.artists.add(newArtist);
        } else {
            artistToEdit.setName(name);
            artistToEdit.setDirection(direction);
            artistToEdit.setDescription(description);
            artistToEdit.setImagePath(selectedImagePath);
        }

        mainController.refreshTableOnly();
        mainController.saveArtistsToJson();
        closeWindow();
    }
    private void cropImageToFit(ImageView imageView, Image image) {
        double targetWidth = 274;
        double targetHeight = 187;

        double sourceWidth = image.getWidth();
        double sourceHeight = image.getHeight();

        double widthRatio = targetWidth / sourceWidth;
        double heightRatio = targetHeight / sourceHeight;
        double scale = Math.max(widthRatio, heightRatio);

        double cropWidth = targetWidth / scale;
        double cropHeight = targetHeight / scale;

        double x = (sourceWidth - cropWidth) / 2;
        double y = (sourceHeight - cropHeight) / 2;

        imageView.setViewport(new javafx.geometry.Rectangle2D(x, y, cropWidth, cropHeight));
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}