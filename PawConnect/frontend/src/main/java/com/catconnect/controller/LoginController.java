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
    @FXML private Button btnGoogleLogin;

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

    @FXML
    private void onGoogleLogin() {
        statusLabel.setText("Opening browser for Google sign-in...");
        new Thread(() -> {
            try {
                // Pick a random free port for the local callback server
                int port;
                try (java.net.ServerSocket s = new java.net.ServerSocket(0)) {
                    port = s.getLocalPort();
                }

                String clientId     = "541589538192-grcf76qdnull6mp78d001nn56la6dlec.apps.googleusercontent.com";
                String clientSecret = "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";
                String redirectUri  = "http://localhost:" + port + "/callback";

                String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                        + "?client_id="     + clientId
                        + "&redirect_uri="  + java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8)
                        + "&response_type=code"
                        + "&scope=email%20profile"
                        + "&prompt=select_account"
                        + "&access_type=offline";

                // Open the system browser (Chrome, Firefox, Edge, etc.)
                java.awt.Desktop.getDesktop().browse(new java.net.URI(authUrl));
                javafx.application.Platform.runLater(() -> statusLabel.setText("Waiting for Google sign-in in your browser..."));

                // Start a tiny local HTTP server to capture the redirect
                com.sun.net.httpserver.HttpServer server =
                        com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(port), 0);

                java.util.concurrent.CompletableFuture<String> codeFuture = new java.util.concurrent.CompletableFuture<>();

                server.createContext("/callback", exchange -> {
                    String query = exchange.getRequestURI().getQuery();
                    String successHtml = "<html><body style='font-family:sans-serif;text-align:center;padding:60px'>"
                            + "<h2 style='color:#7c3aed'>&#10003; Signed in!</h2>"
                            + "<p>You can close this tab and return to PawConnect.</p>"
                            + "</body></html>";
                    String errorHtml = "<html><body style='font-family:sans-serif;text-align:center;padding:60px'>"
                            + "<h2 style='color:#e53e3e'>&#10007; Sign-in cancelled</h2>"
                            + "<p>You can close this tab and try again.</p>"
                            + "</body></html>";

                    if (query != null && query.contains("code=")) {
                        String code = java.util.Arrays.stream(query.split("&"))
                                .filter(s -> s.startsWith("code="))
                                .map(s -> s.substring("code=".length()))
                                .findFirst().orElse(null);
                        byte[] resp = successHtml.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                        exchange.sendResponseHeaders(200, resp.length);
                        exchange.getResponseBody().write(resp);
                        exchange.getResponseBody().close();
                        codeFuture.complete(code);
                    } else {
                        byte[] resp = errorHtml.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                        exchange.sendResponseHeaders(200, resp.length);
                        exchange.getResponseBody().write(resp);
                        exchange.getResponseBody().close();
                        codeFuture.completeExceptionally(new Exception("Google sign-in was cancelled."));
                    }
                    server.stop(1);
                });
                server.start();

                // Wait up to 5 minutes for the user to sign in
                String code = codeFuture.get(5, java.util.concurrent.TimeUnit.MINUTES);

                // Exchange auth code for id_token
                javafx.application.Platform.runLater(() -> statusLabel.setText("Exchanging code for token..."));
                java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();
                String body = "code=" + java.net.URLEncoder.encode(code, java.nio.charset.StandardCharsets.UTF_8)
                        + "&client_id=" + clientId
                        + "&client_secret=" + clientSecret
                        + "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8)
                        + "&grant_type=authorization_code";
                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("https://oauth2.googleapis.com/token"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                        .build();
                java.net.http.HttpResponse<String> res = httpClient.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

                com.fasterxml.jackson.databind.JsonNode json =
                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(res.body());

                if (json.has("id_token")) {
                    String idToken = json.get("id_token").asText();
                    javafx.application.Platform.runLater(() -> statusLabel.setText("Connecting to server..."));
                    AuthResponse response = ApiClient.get().loginWithGoogle(idToken);
                    response.getUser().setIsAdmin(response.getIsAdmin());
                    Session.setAuth(response.getToken(), response.getUser());

                    if (response.getUser().isNewUser()) {
                        javafx.application.Platform.runLater(this::showNicknameDialog);
                    } else {
                        javafx.application.Platform.runLater(this::openMain);
                    }
                } else {
                    String errMsg = json.has("error_description")
                            ? json.get("error_description").asText() : res.body();
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("");
                        UiHelper.showError("Failed to get ID token: " + errMsg);
                    });
                }

            } catch (java.util.concurrent.TimeoutException e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("");
                    UiHelper.showError("Sign-in timed out. Please try again.");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("");
                    UiHelper.showError(e.getMessage() != null ? e.getMessage() : "Google sign-in failed.");
                });
            }
        }).start();
    }


    private void showNicknameDialog() {
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Welcome to PawConnect!");
        dialog.setHeaderText("Set Your Nickname");

        // Styled content
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(12);
        content.setPadding(new javafx.geometry.Insets(20, 20, 10, 20));
        content.setStyle("-fx-background-color: #1a1a2e;");

        javafx.scene.control.Label info = new javafx.scene.control.Label("👋 Welcome! Choose a nickname that others will see on PawConnect.");
        info.setStyle("-fx-text-fill: #b2b2cc; -fx-font-size: 13px; -fx-wrap-text: true;");
        info.setMaxWidth(320);

        javafx.scene.control.TextField nicknameField = new javafx.scene.control.TextField();
        String googleName = Session.getCurrentUser() != null ? Session.getCurrentUser().getDisplayName() : "";
        nicknameField.setText(googleName);
        nicknameField.setPromptText("Your nickname");
        nicknameField.setStyle("-fx-background-color: #16213e; -fx-text-fill: white; -fx-border-color: #7c3aed; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; -fx-font-size: 14px;");

        content.getChildren().addAll(info, nicknameField);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #7c3aed; -fx-border-width: 1;");

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(
                javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL
        );

        // Style the OK button
        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        okButton.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold;");
        okButton.disableProperty().bind(nicknameField.textProperty().length().lessThan(2));

        dialog.setResultConverter(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK) {
                return nicknameField.getText().trim();
            }
            return null;
        });

        java.util.Optional<String> result = dialog.showAndWait();
        String chosen = result.orElse(googleName);
        if (chosen != null && !chosen.isBlank()) {
            // Save nickname in background thread
            new Thread(() -> {
                try {
                    ApiClient.get().updateDisplayName(chosen);
                    Session.getCurrentUser().setDisplayName(chosen);
                } catch (Exception e) {
                    System.err.println("Could not save nickname: " + e.getMessage());
                }
                javafx.application.Platform.runLater(this::openMain);
            }).start();
        } else {
            openMain();
        }
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
