package com.catconnect.controller;

import com.catconnect.model.AuthResponse;
import com.catconnect.service.ApiClient;
import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField displayNameField;
    @FXML private Label statusLabel;
    @FXML private HBox rootPane;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private Button btnBackToLogin;

    @FXML
    public void initialize() {
        statusLabel.setText("");
        new Thread(() -> {
            boolean up = ApiClient.get().isBackendReachable();
            javafx.application.Platform.runLater(() -> {
                if (up) {
                    statusLabel.setStyle("-fx-text-fill: #636e72;");
                } else {
                    statusLabel.setStyle("-fx-text-fill: #c0392b;");
                    statusLabel.setText("Backend OFFLINE — run CatConnectApplication first, then login.");
                }
            });
        }).start();
    }

    @FXML
    private void onLogin() {
        runAuth(false);
    }

    @FXML
    private void onRegister() {
        if (!displayNameField.isVisible()) {
            displayNameField.setVisible(true);
            displayNameField.setManaged(true);
            btnLogin.setVisible(false);
            btnLogin.setManaged(false);
            btnBackToLogin.setVisible(true);
            btnBackToLogin.setManaged(true);
            btnRegister.setText("Confirm Registration");
            return;
        }

        if (displayNameField.getText().isBlank()) {
            statusLabel.setText("Enter display name for registration, then click Confirm Registration again.");
            return;
        }
        
        runAuth(true);
    }

    @FXML
    private void onBackToLogin() {
        displayNameField.setVisible(false);
        displayNameField.setManaged(false);
        displayNameField.clear();
        btnBackToLogin.setVisible(false);
        btnBackToLogin.setManaged(false);
        btnLogin.setVisible(true);
        btnLogin.setManaged(true);
        btnRegister.setText("Create new account");
        statusLabel.setText("");
    }

    private void runAuth(boolean register) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        if (email.isEmpty() || password.length() < 6) {
            UiHelper.showError("Enter valid email and password (min 6 chars).");
            return;
        }
        statusLabel.setText("Connecting...");
        new Thread(() -> {
            try {
                AuthResponse response = register
                        ? ApiClient.get().register(email, password, displayNameField.getText().trim())
                        : ApiClient.get().login(email, password);
                response.getUser().setIsAdmin(response.getIsAdmin());
                Session.setAuth(response.getToken(), response.getUser());
                System.out.println("LOGIN ADMIN = " + Session.getCurrentUser().isAdmin());
                javafx.application.Platform.runLater(this::openMain);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("");
                    UiHelper.showError(e.getMessage());
                });
            }
        }).start();
    }

    private void openMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth() > 0 ? stage.getWidth() : 1100,
                    stage.getHeight() > 0 ? stage.getHeight() : 700);
            var css = getClass().getResource("/styles/app.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
            stage.setTitle("CatConnect");
        } catch (Exception e) {
            String detail = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                detail = detail + " — " + e.getCause().getMessage();
            }
            e.printStackTrace();
            UiHelper.showError("Failed to load app: " + detail);
        }
    }
    private void setupResponsiveFonts() {
        Stage stage = (Stage) emailField.getScene().getWindow();

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();

            if (width < 1200) {
                rootPane.setStyle("""
                -fx-font-size: 12px;
            """);
            } else if (width < 1600) {
                rootPane.setStyle("""
                -fx-font-size: 14px;
            """);
            } else {
                rootPane.setStyle("""
                -fx-font-size: 16px;
            """);
            }
        });
    }
}
