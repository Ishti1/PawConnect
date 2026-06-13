package com.catconnect.controller;

import com.catconnect.service.ApiClient;
import com.catconnect.service.ChatClient;
import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.Map;

public class ChatController {

    @FXML private Label chatStatusLabel;
    @FXML private ListView<String> chatListView;
    @FXML private TextField inputField;
    @FXML private Button sendButton;

    private final ChatClient chatClient = new ChatClient();

    @FXML
    public void initialize() {
        chatStatusLabel.setText("Connecting...");
        chatListView.getItems().clear();
        inputField.setDisable(true);
        sendButton.setDisable(true);

        new Thread(() -> {
            try {
                JsonNode history = ApiClient.get().getList("/chat/messages");
                if (history != null && history.isArray()) {
                    for (JsonNode msg : history) {
                        Platform.runLater(() -> appendChatLine(msg));
                    }
                }

                chatClient.connect(this::appendChatLine);

                Platform.runLater(() -> {
                    chatStatusLabel.setText("Connected — messages update in real time");
                    inputField.setDisable(false);
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> chatStatusLabel.setText("Chat offline: " + safeMessage(e)));
            }
        }).start();

        inputField.setOnAction(e -> onSend());
    }

    @FXML
    private void onSend() {
        String text = inputField.getText() != null ? inputField.getText().trim() : "";
        if (text.isEmpty()) {
            return;
        }
        inputField.setDisable(true);
        sendButton.setDisable(true);

        new Thread(() -> {
            try {
                if (chatClient.isConnected()) {
                    chatClient.send(text);
                } else {
                    ApiClient.get().postJson("/chat/messages",
                            Map.of("content", text, "roomId", "general"));
                }
                Platform.runLater(() -> inputField.clear());
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(safeMessage(ex)));
            } finally {
                Platform.runLater(() -> {
                    inputField.setDisable(false);
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    public void disconnect() {
        chatClient.disconnect();
    }

    private void appendChatLine(JsonNode msg) {
        if (chatListView == null) {
            return;
        }
        String sender = msg.path("senderName").asText("User");
        String content = msg.path("content").asText("");
        String line = sender + ": " + content;
        chatListView.getItems().add(line);
        chatListView.scrollTo(chatListView.getItems().size() - 1);
    }

    private String safeMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "Error";
    }
}

