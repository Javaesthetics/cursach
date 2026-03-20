package com.example.cursach.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddPaintingController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView paintingImageView;
    @FXML private Button saveButton;
    @FXML private Button chooseImageButton;
    @FXML private Button backButton;

    private ArtistController.Artist currentArtist;
    private ArtistController.Painting paintingToEdit;
    private ArtistController mainController;
    private PaintingsController parentController;

    private String selectedImagePath = "";
    private final File paintingsFile = new File("paintings.json");

    public void initData(ArtistController.Artist artist, ArtistController.Painting painting, ArtistController main, PaintingsController parent) {
        this.currentArtist = artist;
        this.paintingToEdit = painting;
        this.mainController = main;
        this.parentController = parent;

        if (backButton != null) {
            backButton.setOnAction(e -> ((Stage) backButton.getScene().getWindow()).close());
        }

        if (paintingToEdit != null) {
            titleField.setText(paintingToEdit.getTitle());
            descriptionArea.setText(paintingToEdit.getDescription());
            selectedImagePath = paintingToEdit.getImagePath();

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                File file = new File(selectedImagePath);
                if (file.exists()) {
                    paintingImageView.setImage(new Image(file.toURI().toString()));
                }
            }
        }

        if (chooseImageButton != null) {
            chooseImageButton.setOnAction(e -> chooseImage());
        }

        if (saveButton != null) {
            saveButton.setOnAction(e -> savePainting());
        }
    }

    private void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Оберіть картину");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(saveButton.getScene().getWindow());

        if (file != null) {
            String savedPath = saveImageToProject(file);
            if (savedPath != null) {
                this.selectedImagePath = savedPath;
                paintingImageView.setImage(new Image(new File(savedPath).toURI().toString()));
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

    private void savePainting() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert("Увага", "Назва картини не може бути порожньою!");
            return;
        }

        if (paintingToEdit == null) {
            paintingToEdit = new ArtistController.Painting();
            paintingToEdit.setArtistId(currentArtist.getId());
        }

        paintingToEdit.setTitle(title);
        paintingToEdit.setDescription(descriptionArea.getText().trim());
        paintingToEdit.setImagePath(selectedImagePath);

        List<ArtistController.Painting> allPaintings = loadAllPaintingsFromFile();

        boolean found = false;
        for (int i = 0; i < allPaintings.size(); i++) {
            if (allPaintings.get(i).getId().equals(paintingToEdit.getId())) {
                allPaintings.set(i, paintingToEdit);
                found = true;
                break;
            }
        }
        if (!found) allPaintings.add(paintingToEdit);
        saveAllPaintingsToFile(allPaintings);
        if (parentController != null) {
            parentController.refresh();
        }

        ((Stage) saveButton.getScene().getWindow()).close();
    }

    private List<ArtistController.Painting> loadAllPaintingsFromFile() {
        if (!paintingsFile.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(paintingsFile)) {
            Type listType = new TypeToken<ArrayList<ArtistController.Painting>>() {}.getType();
            List<ArtistController.Painting> list = new Gson().fromJson(reader, listType);
            return (list != null) ? list : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void saveAllPaintingsToFile(List<ArtistController.Painting> list) {
        try (FileWriter writer = new FileWriter(paintingsFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}