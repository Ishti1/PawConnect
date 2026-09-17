package com.catconnect.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class UiHelper {

    private UiHelper() {}

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("CatConnect");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("CatConnect");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        label.setStyle("-fx-text-fill: #2d3436;");
        return label;
    }

    public static VBox card(String title, String detail) {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label d = new Label(detail);
        d.setWrapText(true);
        d.setStyle("-fx-text-fill: #636e72; -fx-font-size: 12px;");
        box.getChildren().addAll(t, d);
        return box;
    }
}
