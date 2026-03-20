package com.example.cursach.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ArtistController {

    @FXML private ListView<String> artistList;
    @FXML private ImageView artistImage;
    @FXML private TextArea artistDescription;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button addButton, editButton, deleteButton;
    @FXML private Button viewPaintingsButton, reviewsButton;
    @FXML private Button backButton, forwardButton, profileButton;

    public List<Artist> artists = new ArrayList<>();
    private final File artistsFile = new File("artists.json");
    private final File paintingsFile = new File("paintings.json");

    private String currentRole = "user";
    private String currentUsername = "admin";

    private final double TARGET_WIDTH = 290;
    private final double TARGET_HEIGHT = 250;

    @FXML
    void initialize() {
        loadArtistsFromJson();
        updateFilterOptions();
        refreshTableOnly();

        if (!artistList.getItems().isEmpty()) {
            artistList.getSelectionModel().selectFirst();
        }

        artistList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showArtistDetails(newVal);
        });

        filterComboBox.setOnAction(event -> refreshTableOnly());
        addButton.setOnAction(event -> openAddArtistWindow());
        editButton.setOnAction(event -> openEditArtistWindow());
        deleteButton.setOnAction(event -> deleteSelectedArtist());
        viewPaintingsButton.setOnAction(event -> openPaintingsWindow());

        if (reviewsButton != null) reviewsButton.setOnAction(event -> openReviewsWindow());
        if (backButton != null) backButton.setOnAction(event -> goToMainMenu());
        if (forwardButton != null) forwardButton.setOnAction(event -> openPaintingsWindow());
        if (profileButton != null) profileButton.setOnAction(event -> openProfile());

        Platform.runLater(() -> {
            if (artistList.getParent() != null) artistList.getParent().requestFocus();
        });
    }

    private void showArtistDetails(String artistName) {
        artistDescription.clear();
        artistImage.setImage(null);
        artistImage.setViewport(null);

        Artist selectedArtist = getSelectedArtist();
        if (selectedArtist == null) return;

        artistDescription.setText(selectedArtist.getDescription().isEmpty() ? "Біографія відсутня." : selectedArtist.getDescription());

        String path = selectedArtist.getImagePath();
        if (path != null && !path.isBlank()) {
            File imageFile = new File(path);
            if (imageFile.exists()) {
                Image img = new Image(imageFile.toURI().toString());
                artistImage.setImage(img);
                cropImage(artistImage, img);
            }
        }
    }
    private void cropImage(ImageView imageView, Image image) {
        final double TARGET_WIDTH = 316;
        final double TARGET_HEIGHT = 204;

        double imgWidth = image.getWidth();
        double imgHeight = image.getHeight();

        if (imgWidth == 0 || imgHeight == 0) return;

        double scale = Math.min(imgWidth / TARGET_WIDTH, imgHeight / TARGET_HEIGHT);

        double viewWidth = TARGET_WIDTH * scale;
        double viewHeight = TARGET_HEIGHT * scale;

        double x = (imgWidth - viewWidth) / 2;
        double y = (imgHeight - viewHeight) / 2;

        imageView.setViewport(new javafx.geometry.Rectangle2D(x, y, viewWidth, viewHeight));
        imageView.setFitWidth(TARGET_WIDTH);
        imageView.setFitHeight(TARGET_HEIGHT);
        imageView.setPreserveRatio(false);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(TARGET_WIDTH, TARGET_HEIGHT);
        clip.setArcWidth(40.0);
        clip.setArcHeight(40.0);
        imageView.setClip(clip);
    }
    @FXML
    private void handleImageClick(javafx.scene.input.MouseEvent event) {
        if (artistImage.getImage() == null || event.getButton() != javafx.scene.input.MouseButton.PRIMARY) {
            return;
        }

        Stage fullScreenStage = new Stage();
        fullScreenStage.setTitle("Перегляд: " + (getSelectedArtist() != null ? getSelectedArtist().getName() : "Зображення"));

        ImageView fullScreenImageView = new ImageView(artistImage.getImage());
        fullScreenImageView.setPreserveRatio(true);

        fullScreenImageView.setFitHeight(700);

        ScrollPane scrollPane = new ScrollPane(fullScreenImageView);
        scrollPane.setStyle("-fx-background-color: #222; -fx-background: #222;");
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(new BorderPane(scrollPane), 1000, 750);
        fullScreenStage.setScene(scene);

        scene.setOnKeyPressed(e -> fullScreenStage.close());

        fullScreenStage.show();
    }

    private void showWarning(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Попередження");
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane dp = alert.getDialogPane();
        dp.setStyle("-fx-background-color: #FAF0E6; -fx-border-color: #d2b48c; -fx-border-width: 2;");
        dp.lookup(".content.label").setStyle("-fx-text-fill: #793d3d; -fx-font-weight: bold;");

        alert.showAndWait();
    }

    private void openPaintingsWindow() {
        Artist selectedArtist = getSelectedArtist();
        if (selectedArtist == null) {
            showWarning("Художника не обрано", "Будь ласка, виберіть художника зі списку ліворуч.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/paintings.fxml"));
            Parent root = loader.load();
            PaintingsController controller = loader.getController();
            controller.initData(selectedArtist, this, currentUsername);

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("Галерея: " + selectedArtist.getName());
            newStage.show();
            ((Stage) viewPaintingsButton.getScene().getWindow()).close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openReviewsWindow() {
        Artist selectedArtist = getSelectedArtist();
        if (selectedArtist == null) {
            showWarning("Художника не обрано", "Виберіть автора, щоб переглянути відгуки.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/Reviews.fxml"));
            Parent root = loader.load();
            ReviewsController controller = loader.getController();
            controller.initData(null, selectedArtist, currentRole, currentUsername);

            Stage stage = new Stage();
            stage.setTitle("Відгуки: " + selectedArtist.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setUserSession(String username, String role) {
        this.currentUsername = (username != null) ? username : "admin";
        this.currentRole = (role != null) ? role : "user";
        setRole(this.currentRole);
    }

    public void setRole(String role) {
        boolean isAdmin = "admin".equalsIgnoreCase(role);
        if (addButton != null) addButton.setVisible(isAdmin);
        if (editButton != null) editButton.setVisible(isAdmin);
        if (deleteButton != null) deleteButton.setVisible(isAdmin);
    }

    private void deleteSelectedArtist() {
        Artist selectedArtist = getSelectedArtist();
        if (selectedArtist == null) return;

        List<Painting> allPaintings = loadAllPaintingsFromFile();
        allPaintings.removeIf(p -> selectedArtist.getId().equals(p.getArtistId()));
        saveAllPaintingsToFile(allPaintings);

        artists.remove(selectedArtist);
        saveArtistsToJson();
        refreshTableOnly();
        artistImage.setImage(null);
        artistDescription.clear();
    }

    public void refreshTableOnly() {
        artistList.getItems().clear();
        String selectedDirection = filterComboBox.getValue();
        boolean showAll = selectedDirection == null || "Всі".equals(selectedDirection);
        for (Artist artist : artists) {
            if (showAll || (artist.getDirection() != null && artist.getDirection().equals(selectedDirection))) {
                artistList.getItems().add(artist.getName());
            }
        }
    }


    private void loadArtistsFromJson() {
        if (!artistsFile.exists()) return;
        try (FileReader reader = new FileReader(artistsFile)) {
            artists = new Gson().fromJson(reader, new TypeToken<ArrayList<Artist>>() {}.getType());
            if (artists == null) artists = new ArrayList<>();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveArtistsToJson() {
        try (FileWriter writer = new FileWriter(artistsFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(artists, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private List<Painting> loadAllPaintingsFromFile() {
        if (!paintingsFile.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(paintingsFile)) {
            return new Gson().fromJson(reader, new TypeToken<ArrayList<Painting>>() {}.getType());
        } catch (IOException e) { return new ArrayList<>(); }
    }

    public void saveAllPaintingsToFile(List<Painting> list) {
        try (FileWriter writer = new FileWriter(paintingsFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(list, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Artist getSelectedArtist() {
        String name = artistList.getSelectionModel().getSelectedItem();
        return (name == null) ? null : artists.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
    }

    private void updateFilterOptions() {
        List<String> directions = artists.stream()
                .map(Artist::getDirection)
                .filter(d -> d != null && !d.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));

        directions.add(0, "Всі");

        filterComboBox.getItems().setAll(directions);
        filterComboBox.getSelectionModel().select("Всі");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToMainMenu() {
        try {
            Stage s = (Stage) backButton.getScene().getWindow();
            s.setScene(new Scene(new FXMLLoader(getClass().getResource("/com/example/cursach/hello-view.fxml")).load()));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openAddArtistWindow() { openArtistWindow(null); }
    private void openEditArtistWindow() {
        Artist selected = getSelectedArtist();
        if (selected != null) openArtistWindow(selected);
    }

    private void openArtistWindow(Artist edit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/AddArtist.fxml"));
            Parent root = loader.load();
            AddArtistController controller = loader.getController();
            controller.setMainController(this);
            controller.setArtistToEdit(edit);
            Stage s = new Stage();
            s.setScene(new Scene(root));
            s.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static class Artist {
        private String id = java.util.UUID.randomUUID().toString();
        private String name;
        private String direction;
        private String description = "";
        private String imagePath = "";

        public Artist() {}
        public Artist(String name, String direction) {
            this.name = name;
            this.direction = direction;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String n) { name = n; }
        public String getDirection() { return direction; }
        public void setDirection(String d) { direction = d; }
        public String getDescription() { return description; }
        public void setDescription(String d) { description = d; }
        public String getImagePath() { return imagePath; }
        public void setImagePath(String p) { imagePath = p; }
    }

    public static class Painting {
        private String id = java.util.UUID.randomUUID().toString();
        private String artistId;
        private String title;
        private String description;
        private String imagePath;

        public Painting() {}
        public String getId() { return id; }
        public String getArtistId() { return artistId; }
        public void setArtistId(String id) { artistId = id; }
        public String getTitle() { return title; }
        public void setTitle(String t) { title = t; }
        public String getDescription() { return description; }
        public void setDescription(String d) { description = d; }
        public String getImagePath() { return imagePath; }
        public void setImagePath(String p) { imagePath = p; }
    }
}