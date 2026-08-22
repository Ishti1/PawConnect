package com.catconnect.controller;

import com.catconnect.service.ApiClient;
import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ManageAccountController {

    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private Button updateButton;

    @FXML
    public void initialize() {
        if (Session.getCurrentUser() != null) {
            emailField.setText(Session.getCurrentUser().getEmail());
        }
    }

    @FXML
    private void onUpdateAccount() {
        String email = emailField.getText();
        String currentPassword = currentPasswordField.getText();
        String newUsername = newUsernameField.getText();
        String newPassword = newPasswordField.getText();

        if (email == null || email.isBlank()) {
            UiHelper.showError("Email is required for authentication.");
            return;
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            UiHelper.showError("Current password is required to make changes.");
            return;
        }
        
        if ((newUsername == null || newUsername.isBlank()) && (newPassword == null || newPassword.isBlank())) {
            UiHelper.showError("Please provide a new username or password to update.");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("email", email.trim());
        body.put("currentPassword", currentPassword);
        
        if (newUsername != null && !newUsername.isBlank()) {
            body.put("newDisplayName", newUsername.trim());
        }
        if (newPassword != null && !newPassword.isBlank()) {
            body.put("newPassword", newPassword);
        }

        updateButton.setDisable(true);
        updateButton.setText("Updating...");

        CompletableFuture.runAsync(() -> {
            try {
                JsonNode json = ApiClient.get().putJson("/auth/manage", (Map<String, Object>)(Map)body);
                
                Platform.runLater(() -> {
                    updateButton.setDisable(false);
                    updateButton.setText("Update Account");
                    
                    try {
                        String token = json.path("token").asText();
                        
                        // Rebuild User Session object
                        JsonNode userNode = json.path("user");
                        com.catconnect.model.User updatedUser = new com.catconnect.model.User();
                        updatedUser.setId(userNode.path("id").asLong());
                        updatedUser.setEmail(userNode.path("email").asText());
                        updatedUser.setDisplayName(userNode.path("displayName").asText());
                        updatedUser.setIsAdmin(json.path("isAdmin").asBoolean());
                        
                        Session.setAuth(token, updatedUser);
                        
                        if (MainController.getInstance() != null) {
                            MainController.getInstance().updateUserLabel();
                        }
                        
                        UiHelper.showInfo("Account updated successfully!");
                        
                        // Clear passwords
                        currentPasswordField.clear();
                        newPasswordField.clear();
                        newUsernameField.clear();
                        
                    } catch (Exception e) {
                        UiHelper.showError("Failed to parse response.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateButton.setDisable(false);
                    updateButton.setText("Update Account");
                    UiHelper.showError("Network error: " + e.getMessage());
                });
            }
        });
    }
}
