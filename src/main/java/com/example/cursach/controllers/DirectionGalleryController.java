package com.example.cursach.controllers;

import com.example.cursach.controllers.ArtistController.Painting;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectionGalleryController {

    @FXML private Button backButton;
    @FXML private Text headerText;
    @FXML private ListView<String> paintingList;
    @FXML private ImageView paintingImage;
    @FXML private TextArea paintingDescription;
    @FXML private Button profileButton;
    @FXML private Button reviewButton;
    @FXML private Button addToProfileButton;

    private final File paintingsFile = new File("paintings.json");
    private final File favoritesFile = new File("favorites.json");
    private Map<String, Painting> paintingMap = new HashMap<>();

    private String currentUsername = "Анонім";
    private String currentDirection;
    private String currentArtistId;

    // 🔥 Розміри під твою нову широку рамку (350x250 як ми робили раніше)
    private final double TARGET_WIDTH = 350;
    private final double TARGET_HEIGHT = 250;

    @FXML
    void initialize() {
        // Знімаємо фокус з елементів при старті
        if (paintingList != null) {
            paintingList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) showPaintingDetails(newVal);
            });
        }

        // Встановлюємо дії для кнопок
        if (profileButton != null) profileButton.setOnAction(e -> openProfile());
        if (reviewButton != null) reviewButton.setOnAction(e -> openReviews());
        if (addToProfileButton != null) addToProfileButton.setOnAction(e -> addToFavorites());
    }

    // --- МОДЕЛЬ ОБРАНОГО (Повинна збігатися з ProfileController) ---
    public static class FavoriteItem {
        private String username;
        private String paintingTitle;
        private String artistId;

        public FavoriteItem(String username, String paintingTitle, String artistId) {
            this.username = username;
            this.paintingTitle = paintingTitle;
            this.artistId = artistId;
        }
        public String getUsername() { return username; }
        public String getPaintingTitle() { return paintingTitle; }
        public String getArtistId() { return artistId; }
    }

    // --- ТЕХНІЧНІ МЕТОДИ ІНІЦІАЛІЗАЦІЇ ---
    public void initData(String direction, String artistName, String artistId, String username) {
        this.currentDirection = direction;
        this.currentArtistId = artistId;
        this.currentUsername = (username != null && !username.isEmpty()) ? username : "Анонім";

        if (headerText != null) headerText.setText("Художник: " + artistName);

        loadPaintingsFromTable(artistId);

        if (backButton != null) backButton.setOnAction(event -> goBack());

        // Автоматично обираємо першу картину, щоб рамка не була порожньою
        if (!paintingList.getItems().isEmpty()) {
            paintingList.getSelectionModel().selectFirst();
        }
    }

    private void loadPaintingsFromTable(String targetArtistId) {
        paintingList.getItems().clear();
        paintingMap.clear();
        List<Painting> allPaintings = loadAllPaintings();
        if (allPaintings != null) {
            for (Painting p : allPaintings) {
                if (p.getArtistId() != null && p.getArtistId().equals(targetArtistId)) {
                    paintingList.getItems().add(p.getTitle());
                    paintingMap.put(p.getTitle(), p);
                }
            }
        }
    }

    private List<Painting> loadAllPaintings() {
        if (!paintingsFile.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(paintingsFile)) {
            Type listType = new TypeToken<ArrayList<Painting>>(){}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (IOException e) { return new ArrayList<>(); }
    }

    private void showPaintingDetails(String title) {
        Painting p = paintingMap.get(title);
        if (p != null && p.getImagePath() != null) {
            File file = new File(p.getImagePath());
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                paintingImage.setImage(img);

                // Викликаємо метод обрізки після встановлення картинки
                cropImage(paintingImage, img);

                paintingDescription.setText(p.getDescription());
            }
        }
    }

    /**
     * Метод для ідеального заповнення рамки зображенням (Center Crop).
     * Використовує нові публічні геттери класу Painting.
     */
    private void cropImage(ImageView imageView, Image image) {
        // Розміри мають точно збігатися з розмірами у FXML вище
        final double TARGET_WIDTH = 320.0;
        final double TARGET_HEIGHT = 200.0;

        double imgWidth = image.getWidth();
        double imgHeight = image.getHeight();

        if (imgWidth == 0 || imgHeight == 0) return;

        // Розраховуємо коефіцієнт масштабування (Center Crop)
        double scale = Math.min(imgWidth / TARGET_WIDTH, imgHeight / TARGET_HEIGHT);

        double viewWidth = TARGET_WIDTH * scale;
        double viewHeight = TARGET_HEIGHT * scale;

        // Центруємо область обрізки на оригінальному файлі
        double x = (imgWidth - viewWidth) / 2;
        double y = (imgHeight - viewHeight) / 2;

        // Застосовуємо обрізку та розміри відображення
        imageView.setViewport(new Rectangle2D(x, y, viewWidth, viewHeight));
        imageView.setFitWidth(TARGET_WIDTH);
        imageView.setFitHeight(TARGET_HEIGHT);
        imageView.setPreserveRatio(false);

        // Додатково: програмне закруглення кутів для ImageView
        Rectangle clip = new Rectangle(TARGET_WIDTH, TARGET_HEIGHT);
        clip.setArcWidth(30.0);
        clip.setArcHeight(30.0);
        imageView.setClip(clip);
    }

    @FXML
    public void handleImageClick() {
        if (paintingImage == null || paintingImage.getImage() == null) return;

        Image image = paintingImage.getImage();
        Stage fullScreenStage = new Stage();
        fullScreenStage.setTitle("Перегляд картини");

        ImageView fullView = new ImageView(image);
        fullView.setPreserveRatio(true);
        fullView.setFitWidth(1000);

        ScrollPane scrollPane = new ScrollPane(fullView);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: #222;");

        fullScreenStage.setScene(new Scene(new BorderPane(scrollPane), 1050, 750));
        fullScreenStage.show();
    }

    // --- ЛОГІКА ОБРАНОГО ---
    private void addToFavorites() {
        String selectedTitle = paintingList.getSelectionModel().getSelectedItem();
        if (selectedTitle == null) {
            showAlert("Увага", "Будь ласка, оберіть картину зі списку!", Alert.AlertType.WARNING);
            return;
        }

        Painting selectedPainting = paintingMap.get(selectedTitle);
        String artistId = (selectedPainting != null) ? selectedPainting.getArtistId() : currentArtistId;

        List<FavoriteItem> favorites = new ArrayList<>();
        Gson gson = new Gson();

        if (favoritesFile.exists()) {
            try (FileReader reader = new FileReader(favoritesFile)) {
                Type listType = new TypeToken<ArrayList<FavoriteItem>>() {}.getType();
                List<FavoriteItem> loaded = gson.fromJson(reader, listType);
                if (loaded != null) favorites = loaded;
            } catch (IOException e) { e.printStackTrace(); }
        }

        boolean alreadyExists = favorites.stream().anyMatch(f ->
                f.getUsername().equals(currentUsername) && f.getPaintingTitle().equals(selectedTitle));

        if (alreadyExists) {
            showAlert("Інформація", "Ця картина вже є у вашому профілі.", Alert.AlertType.INFORMATION);
            return;
        }

        favorites.add(new FavoriteItem(currentUsername, selectedTitle, artistId));

        try (FileWriter writer = new FileWriter(favoritesFile)) {
            gson.toJson(favorites, writer);
            showAlert("Успіх", "Картину додано в обране!", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Помилка", "Не вдалося зберегти зміни.", Alert.AlertType.ERROR);
        }
    }

    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/Profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.initData(currentUsername, "user");

            Stage stage = new Stage();
            stage.setTitle("Профіль: " + currentUsername);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Помилка", "Не вдалося відкрити профіль.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/zalaArtist.fxml"));
            Parent root = loader.load();
            ZalaArtistController controller = loader.getController();
            controller.setInfo(currentDirection, currentUsername);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openReviews() {
        // 1. Отримуємо назву обраної картини зі списку
        String selectedTitle = paintingList.getSelectionModel().getSelectedItem();

        // 2. Перевіряємо, чи взагалі щось обрано
        if (selectedTitle == null) {
            showAlert("Увага", "Будь ласка, спочатку оберіть картину, щоб переглянути відгуки про неї.", Alert.AlertType.WARNING);
            return;
        }

        // 3. Знаходимо об'єкт картини в мапі
        Painting selectedPainting = paintingMap.get(selectedTitle);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cursach/Reviews.fxml"));
            Parent root = loader.load();

            ReviewsController controller = loader.getController();

            // 🔥 ПЕРЕДАЄМО КАРТИНУ: перший параметр - картина, другий (автор) - null
            // Це дасть зрозуміти контролеру відгуків, що ми працюємо саме з картинною
            controller.initData(selectedPainting, null, "user", currentUsername);

            Stage stage = new Stage();
            stage.setTitle("Відгуки про картину: " + selectedPainting.getTitle());
            stage.setScene(new Scene(root));

            // Модальне вікно, щоб користувач фокусувався на відгуках
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Помилка", "Не вдалося відкрити вікно відгуків.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // Стилізація під твій дизайн
        alert.getDialogPane().setStyle("-fx-background-color: #FAF0E6;");
        alert.showAndWait();
    }
}