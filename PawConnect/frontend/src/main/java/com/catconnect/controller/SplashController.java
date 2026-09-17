package com.catconnect.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {

    @FXML
    public void initialize() {

        PauseTransition delay = new PauseTransition(Duration.seconds(2));

        delay.setOnFinished(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) javafx.stage.Window.getWindows().get(0);

                double width = stage.getScene().getWidth();
                double height = stage.getScene().getHeight();

                Scene scene = new Scene(root, width, height);

                var css = getClass().getResource("/styles/app.css");
                if (css != null) {
                    scene.getStylesheets().add(css.toExternalForm());
                }

                stage.setScene(scene);
                stage.setTitle("PaWConnect");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        delay.play();
    }
}