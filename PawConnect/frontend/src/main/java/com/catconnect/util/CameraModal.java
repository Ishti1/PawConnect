package com.catconnect.util;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class CameraModal {

    public static void show(Stage owner, Consumer<File> onCapture) {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            UiHelper.showError("No webcam detected on this device.");
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Take Photo");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(640);
        imageView.setFitHeight(480);
        imageView.setPreserveRatio(true);

        Button captureButton = new Button("📸 Capture");
        captureButton.getStyleClass().add("primary-button");

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("secondary-button");

        HBox buttons = new HBox(10, captureButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(10, imageView, buttons);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-background-color: #222222;");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(CameraModal.class.getResource("/styles/app.css").toExternalForm());
        stage.setScene(scene);

        AtomicBoolean isCapturing = new AtomicBoolean(true);

        Thread videoThread = new Thread(() -> {
            webcam.open();
            while (isCapturing.get()) {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    Image fxImage = SwingFXUtils.toFXImage(image, null);
                    Platform.runLater(() -> imageView.setImage(fxImage));
                }
                try {
                    Thread.sleep(33); // ~30 FPS
                } catch (InterruptedException e) {
                    break;
                }
            }
            webcam.close();
        });
        videoThread.setDaemon(true);
        videoThread.start();

        stage.setOnCloseRequest(e -> {
            isCapturing.set(false);
        });

        cancelButton.setOnAction(e -> {
            isCapturing.set(false);
            stage.close();
        });

        captureButton.setOnAction(e -> {
            isCapturing.set(false);
            BufferedImage image = webcam.getImage();
            stage.close();

            if (image != null) {
                try {
                    File tempFile = File.createTempFile("webcam_capture", ".jpg");
                    ImageIO.write(image, "JPG", tempFile);
                    onCapture.accept(tempFile);
                } catch (IOException ex) {
                    UiHelper.showError("Could not save captured image: " + ex.getMessage());
                }
            }
        });

        stage.show();
    }
}
