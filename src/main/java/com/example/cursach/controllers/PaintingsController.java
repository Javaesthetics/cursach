package com.example.cursach.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaintingsController {

    @FXML private ListView<String> paintingList;
    @FXML private ImageView paintingImage;
    @FXML private TextArea paintingDescription;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;
    @FXML private Button profileButton;
    @FXML private Button vidguku;

    private ArtistController.Artist currentArtist;
    private ArtistController mainController;
    private String currentUsername = "admin";

    private final File paintingsFile = new File("paintings.json");
    private List<ArtistController.Painting> artistPaintings = new ArrayList<>();

    @FXML
    void initialize() {
        paintingList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            showPaintingDetails(newVal);
        });

        if (profileButton != null) {
            profileButton.setOnAction(e -> openProfile());
        }

        if (vidguku != null) {
            vidguku.setOnAction(e -> openReviewsForAdmin());
        }
    }

    public void initData(ArtistController.Artist artist, ArtistController main, String username) {
        this.currentArtist = artist;
        this.mainController = main;
        this.currentUsername = (username != null) ? username : "admin";

        refresh();

        addButton.setOnAction(e -> openAddPaintingWindow(null));
        editButton.setOnAction(e -> {
            ArtistController.Painting p = getSelectedPainting();
            if (p != null) openAddPaintingWindow(p);
            else showAlert("Увага", "Оберіть картину для редагування!");
        });
        deleteButton.setOnAction(e -> deletePainting());
        backButton.setOnAction(e -> goBack());
    }

    private void openReviewsForAdmin() {
        ArtistController.Painting selectedPainting = getSelectedPainting();
        if (selectedPainting == null) {
            showAlert("Увага", "Будь ласка, спочатку оберіть картину у списку!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/ReviewsAdmin.fxml"));
            Parent root = loader.load();

            ReviewsAdminController controller = loader.getController();
            controller.initData(selectedPainting, currentUsername);

            Stage stage = new Stage();
            stage.setTitle("Модерація відгуків: " + selectedPainting.getTitle());
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Помилка", "Не вдалося завантажити ReviewsAdmin.fxml.");
        }
    }

    public void refresh() {
        paintingList.getItems().clear();
        List<ArtistController.Painting> allPaintings = loadAllPaintingsFromFile();
        artistPaintings = allPaintings.stream()
                .filter(p -> p.getArtistId() != null && p.getArtistId().equals(currentArtist.getId()))
                .collect(Collectors.toList());
        for (ArtistController.Painting p : artistPaintings) {
            paintingList.getItems().add(p.getTitle());
        }
    }

    private List<ArtistController.Painting> loadAllPaintingsFromFile() {
        if (!paintingsFile.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(paintingsFile)) {
            Type listType = new TypeToken<ArrayList<ArtistController.Painting>>() {}.getType();
            List<ArtistController.Painting> list = new Gson().fromJson(reader, listType);
            return (list != null) ? list : new ArrayList<>();
        } catch (IOException e) { return new ArrayList<>(); }
    }

    private void showPaintingDetails(String title) {
        paintingDescription.clear();
        paintingImage.setImage(null);
        paintingImage.setViewport(null);

        ArtistController.Painting p = getSelectedPainting();
        if (p != null) {
            paintingDescription.setText(p.getDescription());
            if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
                File file = new File(p.getImagePath());
                if (file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    paintingImage.setImage(img);
                    cropImage(paintingImage, img);
                }
            }
        }
    }

    private void deletePainting() {
        ArtistController.Painting p = getSelectedPainting();
        if (p == null) {
            showAlert("Помилка", "Оберіть картину для видалення!");
            return;
        }
        List<ArtistController.Painting> all = loadAllPaintingsFromFile();
        all.removeIf(item -> item.getId().equals(p.getId()));
        mainController.saveAllPaintingsToFile(all);
        refresh();
        paintingImage.setImage(null);
        paintingDescription.clear();
    }

    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/ProfileAdmin.fxml"));
            Parent root = loader.load();
            ProfileAdminController controller = loader.getController();
            controller.initData(currentUsername);
            Stage stage = new Stage();
            stage.setTitle("Профіль адміністратора");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openAddPaintingWindow(ArtistController.Painting paintingToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/AddPainting.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(paintingToEdit == null ? "Додати картину" : "Редагувати картину");
            stage.setScene(new Scene(root));
            AddPaintingController controller = loader.getController();
            controller.initData(currentArtist, paintingToEdit, mainController, this);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/artist.fxml"));
            Parent root = loader.load();
            ArtistController controller = loader.getController();
            controller.setUserSession(currentUsername, "admin");
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void cropImage(ImageView imageView, Image image) {
        final double TARGET_WIDTH = 280.0;
        final double TARGET_HEIGHT = 200.0;
        double imgWidth = image.getWidth();
        double imgHeight = image.getHeight();
        if (imgWidth == 0 || imgHeight == 0) return;
        double scale = Math.min(imgWidth / TARGET_WIDTH, imgHeight / TARGET_HEIGHT);
        double viewWidth = TARGET_WIDTH * scale;
        double viewHeight = TARGET_HEIGHT * scale;
        double x = (imgWidth - viewWidth) / 2;
        double y = (imgHeight - viewHeight) / 2;
        imageView.setViewport(new Rectangle2D(x, y, viewWidth, viewHeight));
        imageView.setFitWidth(TARGET_WIDTH);
        imageView.setFitHeight(TARGET_HEIGHT);
        imageView.setPreserveRatio(false);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(TARGET_WIDTH, TARGET_HEIGHT);
        clip.setArcWidth(30.0); clip.setArcHeight(30.0);
        imageView.setClip(clip);
    }

    private ArtistController.Painting getSelectedPainting() {
        String title = paintingList.getSelectionModel().getSelectedItem();
        if (title == null) return null;
        return artistPaintings.stream()
                .filter(p -> p.getTitle().equals(title))
                .findFirst().orElse(null);
    }

    private void showAlert(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(text);
        alert.showAndWait();
    }

    @FXML
    private void handleImageClick() {
        if (paintingImage.getImage() == null) return;
        Image image = paintingImage.getImage();
        Stage fullScreenStage = new Stage();
        fullScreenStage.setTitle("Перегляд зображення");
        ImageView fullScreenImageView = new ImageView(image);
        fullScreenImageView.setPreserveRatio(true);
        fullScreenImageView.setFitHeight(800); fullScreenImageView.setFitWidth(1200);
        ScrollPane scrollPane = new ScrollPane(fullScreenImageView);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: #222;");
        fullScreenStage.setScene(new Scene(new BorderPane(scrollPane), 1000, 750));
        fullScreenStage.show();
    }
}