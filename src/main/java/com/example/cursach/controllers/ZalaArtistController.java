package com.example.cursach.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ZalaArtistController {

    @FXML private ListView<String> vvv;
    @FXML private Button nextButton;
    @FXML private Button backButton;
    @FXML private Button profileButton;
    @FXML private Text titleText;
    @FXML private ImageView artistImage;
    @FXML private TextArea artistDescription;

    private String currentDirection;
    private String currentUsername;
    private List<ArtistController.Artist> allArtists = new ArrayList<>();
    private final File artistsFile = new File("artists.json");

    @FXML
    void initialize() {
        nextButton.setOnAction(event -> goToGallery());
        backButton.setOnAction(event -> goBack());

        if (profileButton != null) {
            profileButton.setOnAction(e -> openProfile());
        }

        vvv.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showArtistDetails(newVal);
        });
    }

    public void setInfo(String direction, String username) {
        this.currentDirection = direction;
        this.currentUsername = username;
        if (titleText != null) titleText.setText("Зала: " + direction);
        loadArtistsByDirection(direction);
    }

    private void loadArtistsByDirection(String direction) {
        vvv.getItems().clear();
        allArtists = loadAllArtists();
        List<String> filteredNames = allArtists.stream()
                .filter(a -> a.getDirection() != null && a.getDirection().equalsIgnoreCase(direction))
                .map(ArtistController.Artist::getName)
                .collect(Collectors.toList());
        vvv.getItems().addAll(filteredNames);
    }

    private void showArtistDetails(String name) {
        ArtistController.Artist artist = findArtistByName(name);
        if (artist != null) {
            artistDescription.setText(artist.getDescription());
            if (artist.getImagePath() != null && !artist.getImagePath().isEmpty()) {
                File file = new File(artist.getImagePath());
                if (file.exists()) {
                    Image img = new Image(file.toURI().toString(), true);
                    img.progressProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() == 1.0) {
                            artistImage.setImage(img);
                            cropImage(artistImage, img);
                        }
                    });
                    if (!img.isBackgroundLoading() && img.getProgress() == 1.0) {
                        artistImage.setImage(img);
                        cropImage(artistImage, img);
                    }
                }
            } else {
                artistImage.setImage(null);
                artistImage.setViewport(null);
            }
        }
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
        clip.setArcWidth(30.0);
        clip.setArcHeight(30.0);
        imageView.setClip(clip);
    }

    @FXML
    private void handleImageClick() {
        if (artistImage == null || artistImage.getImage() == null) return;
        Image image = artistImage.getImage();
        Stage fullScreenStage = new Stage();
        fullScreenStage.setTitle("Перегляд зображення");

        ImageView fullView = new ImageView(image);
        fullView.setPreserveRatio(true);
        fullView.setFitWidth(1000);

        ScrollPane scrollPane = new ScrollPane(fullView);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: #222; -fx-background: #222;");

        fullScreenStage.setScene(new Scene(new BorderPane(scrollPane), 1050, 750));
        fullScreenStage.show();
    }

    private void goToGallery() {
        String selectedName = vvv.getSelectionModel().getSelectedItem();
        if (selectedName == null) {
            showAlert("Увага", "Будь ласка, оберіть художника!");
            return;
        }
        ArtistController.Artist artist = findArtistByName(selectedName);
        if (artist != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/DirectionGallery.fxml"));
                Parent root = loader.load();
                DirectionGalleryController controller = loader.getController();
                controller.initData(currentDirection, artist.getName(), artist.getId(), currentUsername);
                Stage stage = (Stage) nextButton.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private ArtistController.Artist findArtistByName(String name) {
        return allArtists.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
    }

    private List<ArtistController.Artist> loadAllArtists() {
        if (!artistsFile.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(artistsFile)) {
            Type listType = new TypeToken<ArrayList<ArtistController.Artist>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (IOException e) { return new ArrayList<>(); }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/napriam-view.fxml"));
            Parent root = loader.load();
            NapriamController controller = loader.getController();
            controller.setDisplayName(currentUsername);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/Profile.fxml"));
            Parent root = loader.load();
            ProfileController pc = loader.getController();
            pc.initData(currentUsername, "user");
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}