package com.example.cursach.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CustomAlertController {

    @FXML private Text titleText;
    @FXML private Label messageLabel;
    @FXML private Button okButton;

    @FXML
    public void initialize() {
        okButton.setOnAction(event -> {
            Stage stage = (Stage) okButton.getScene().getWindow();
            stage.close();
        });
    }
    public void setInfo(String title, String message) {
        titleText.setText(title);
        messageLabel.setText(message);
    }
}