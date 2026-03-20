module com.example.cursach {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires com.google.gson;

    // Відкрити пакети для FXML і Gson
    opens com.example.cursach to javafx.fxml;

    // Контролери відкриті для FXML та Gson
    opens com.example.cursach.controllers to javafx.fxml, com.google.gson;

    // 👇👇👇 ОСЬ ЦЬОГО РЯДКА НЕ ВИСТАЧАЛО 👇👇👇
    // Відкриваємо моделі (Review.java) для Gson, щоб він міг їх читати/писати
    opens com.example.cursach.models to com.google.gson;

    // Експортуємо пакети
    exports com.example.cursach;
    exports com.example.cursach.controllers;
}