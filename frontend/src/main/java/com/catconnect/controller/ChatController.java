package com.catconnect.controller;

import com.catconnect.service.ApiClient;
import com.catconnect.service.ChatClient;
import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ChatController {

    @FXML private Label screenTitle;
    @FXML private Label chatStatusLabel;
    @FXML private Label statusDot;
    @FXML private VBox contactPanel;
    @FXML private ListView<ChatMessage> chatListView;
    @FXML private TextField inputField;
    @FXML private Button sendButton;
    @FXML private Button btnCommunityTab;
    @FXML private Button btnDirectTab;
    @FXML private ListView<String> userListView;
    @FXML private Button photoButton;
    @FXML private HBox chatHeaderBox;
    @FXML private StackPane chatHeaderAvatar;
    @FXML private Label chatHeaderName;
    @FXML private HBox actionButtonsBox;
    @FXML private Button heartButton;
    @FXML private Button micButton;
    @FXML private Button emojiButton;
    @FXML private HBox emojiPaletteBox;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ChatClient chatClient = new ChatClient();
    private String currentRoomId = "general";
    private Long lastMessagedUserId = null;
    private String lastMessagedUserName = "User";
    private final Map<String, Long> nameToIdMap = new HashMap<>();
    private LocalDate lastMessageDate = null;

    @FXML
    public void initialize() {
        chatListView.setCellFactory(param -> new ChatCell());
        chatListView.setPlaceholder(placeholderLabel("No messages yet — say hi 👋"));

        userListView.setCellFactory(param -> new ContactCell());
        userListView.setPlaceholder(placeholderLabel("No contacts yet"));

        openCommunityChat();
        inputField.setOnAction(e -> onSend());
        
        inputField.textProperty().addListener((obs, oldText, newText) -> {
            boolean hasText = newText != null && !newText.trim().isEmpty();
            actionButtonsBox.setVisible(!hasText);
            actionButtonsBox.setManaged(!hasText);
            sendButton.setVisible(hasText);
            sendButton.setManaged(hasText);
        });

        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Long id = nameToIdMap.get(newVal);
                if (id != null) openChatWithUser(id, newVal);
            }
        });
        
        setupEmojiPalette();
    }

    private void setupEmojiPalette() {
        if (emojiPaletteBox == null) return;
        String[] emojis = {"😀", "😂", "😍", "🥺", "😭", "😡", "🐾"};
        for (String emoji : emojis) {
            Button btn = new Button();
            btn.getStyleClass().add("reaction-btn");
            
            ImageView imgView = new ImageView();
            imgView.setFitWidth(20);
            imgView.setFitHeight(20);
            ApiClient.get().loadImageAsync(getTwemojiUrl(emoji), imgView);
            btn.setGraphic(imgView);
            
            btn.setOnAction(e -> {
                inputField.appendText(emoji);
                inputField.requestFocus();
                emojiPaletteBox.setVisible(false);
                emojiPaletteBox.setManaged(false);
            });
            emojiPaletteBox.getChildren().add(btn);
        }
    }
    
    private String getTwemojiUrl(String emoji) {
        String code;
        switch (emoji) {
            case "😀": code = "1f600"; break;
            case "😂": code = "1f602"; break;
            case "😍": code = "1f60d"; break;
            case "🥺": code = "1f97a"; break;
            case "😭": code = "1f62d"; break;
            case "😡": code = "1f621"; break;
            case "🐾": code = "1f43e"; break;
            case "👍": code = "1f44d"; break;
            case "❤️": code = "2764"; break;
            default: code = "1f600"; break;
        }
        return "https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + code + ".png";
    }

    private javafx.scene.text.TextFlow createEmojiTextFlow(String text) {
        javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(😀|😂|😍|🥺|😭|😡|🐾|👍|❤️)");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                javafx.scene.text.Text t = new javafx.scene.text.Text(text.substring(lastEnd, matcher.start()));
                t.setFill(javafx.scene.paint.Color.valueOf("#2d3436"));
                flow.getChildren().add(t);
            }
            String emoji = matcher.group(1);
            ImageView img = new ImageView();
            img.setFitWidth(16);
            img.setFitHeight(16);
            
            // Fix vertical alignment for inline images in TextFlow
            javafx.scene.layout.VBox imgContainer = new javafx.scene.layout.VBox(img);
            imgContainer.setAlignment(Pos.CENTER);
            imgContainer.setPadding(new javafx.geometry.Insets(0, 1, -3, 1));
            
            ApiClient.get().loadImageAsync(getTwemojiUrl(emoji), img);
            flow.getChildren().add(imgContainer);
            lastEnd = matcher.end();
        }
        if (lastEnd < text.length()) {
            javafx.scene.text.Text t = new javafx.scene.text.Text(text.substring(lastEnd));
            t.setFill(javafx.scene.paint.Color.valueOf("#2d3436"));
            flow.getChildren().add(t);
        }
        return flow;
    }

    private Label placeholderLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("empty-label");
        return lbl;
    }

    @FXML
    private void onCommunityTabClick() {
        openCommunityChat();
    }

    @FXML
    private void onDirectTabClick() {
        contactPanel.setVisible(true);
        contactPanel.setManaged(true);

        if (userListView.getItems().isEmpty()) {
            loadChatContacts();
        }

        updateTabStyles(btnDirectTab, btnCommunityTab);

        if (lastMessagedUserId != null) {
            openChatWithUser(lastMessagedUserId, lastMessagedUserName);
        } else {
            showEmptyDirectMessageState();
        }
    }

    public void openCommunityChat() {
        updateTabStyles(btnCommunityTab, btnDirectTab);
        contactPanel.setVisible(false);
        contactPanel.setManaged(false);

        setupHeader("Community Chat");
        loadRoom("general", "Community Chat");
    }

    private void loadChatContacts() {
        new Thread(() -> {
            try {
                JsonNode contacts = ApiClient.get().getList("/chat/contacts");
                Platform.runLater(() -> {
                    userListView.getItems().clear();
                    nameToIdMap.clear();
                    if (contacts != null && contacts.isArray()) {
                        for (JsonNode contact : contacts) {
                            String name = contact.path("displayName").asText();
                            Long id = contact.path("id").asLong();
                            nameToIdMap.put(name, id);
                            userListView.getItems().add(name);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> UiHelper.showError("Failed to load contacts."));
            }
        }).start();
    }

    public void openChatWithUser(Long targetUserId, String userName) {
        this.lastMessagedUserId = targetUserId;
        this.lastMessagedUserName = userName;
        updateTabStyles(btnDirectTab, btnCommunityTab);
        setupHeader(userName);
        loadRoom(dmRoomId(targetUserId), "Direct Message: " + userName);
    }
    
    private void setupHeader(String name) {
        chatHeaderBox.setVisible(true);
        chatHeaderBox.setManaged(true);
        chatHeaderName.setText(name);
        chatHeaderAvatar.getChildren().clear();
        String initial = (name == null || name.isBlank()) ? "?" : name.trim().substring(0, 1).toUpperCase();
        chatHeaderAvatar.getChildren().add(new Label(initial));
    }

    private String dmRoomId(Long targetUserId) {
        Long selfId = Session.getCurrentUser() != null ? Session.getCurrentUser().getId() : null;
        if (selfId == null) {
            return "dm_" + targetUserId;
        }
        long lo = Math.min(selfId, targetUserId);
        long hi = Math.max(selfId, targetUserId);
        return "dm_" + lo + "_" + hi;
    }

    private void showEmptyDirectMessageState() {
        updateTabStyles(btnDirectTab, btnCommunityTab);
        this.currentRoomId = "dm_empty";
        setStatusOffline();

        Platform.runLater(() -> {
            if (screenTitle != null) screenTitle.setText("Direct Messages");
            chatStatusLabel.setText("No active chat");
            chatListView.getItems().clear();
            chatListView.setPlaceholder(placeholderLabel("Select a contact to start a direct message"));
            inputField.setDisable(true);
            chatHeaderBox.setVisible(false);
            chatHeaderBox.setManaged(false);
        });
    }

    private void loadRoom(String roomId, String title) {
        this.currentRoomId = roomId;
        Platform.runLater(() -> {
            lastMessageDate = null;
            if (screenTitle != null) screenTitle.setText(title);
            chatStatusLabel.setText("Connecting...");
            setStatusOffline();
            chatListView.setPlaceholder(placeholderLabel("No messages yet — say hi 👋"));
            chatListView.getItems().clear();
            inputField.setDisable(true);
        });

        new Thread(() -> {
            try {
                String endpoint = "/chat/messages";
                if (!roomId.equals("general")) {
                    endpoint += "?roomId=" + URLEncoder.encode(roomId, StandardCharsets.UTF_8);
                }
                JsonNode history = ApiClient.get().getList(endpoint);

                chatClient.ensureConnected(this::handleIncomingMessage);
                chatClient.subscribe(roomId);

                Platform.runLater(() -> {
                    chatListView.getItems().clear();
                    if (history != null && history.isArray()) {
                        for (JsonNode msg : history) appendChatLine(msg, false);
                    }
                    chatStatusLabel.setText("Connected");
                    setStatusOnline();
                    inputField.setDisable(false);
                    if (!chatListView.getItems().isEmpty()) {
                        // Pause for 100ms to allow all chat bubbles to calculate their height, then scroll
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
                        pause.setOnFinished(ev -> chatListView.scrollTo(chatListView.getItems().size() - 1));
                        pause.play();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatStatusLabel.setText("Offline: " + safeMessage(e));
                    setStatusOffline();
                });
            }
        }).start();
    }

    private void setStatusOnline() {
        if (statusDot != null) {
            statusDot.getStyleClass().removeAll("status-dot-offline");
            if (!statusDot.getStyleClass().contains("status-dot-online")) {
                statusDot.getStyleClass().add("status-dot-online");
            }
        }
    }

    private void setStatusOffline() {
        if (statusDot != null) {
            statusDot.getStyleClass().removeAll("status-dot-online");
            if (!statusDot.getStyleClass().contains("status-dot-offline")) {
                statusDot.getStyleClass().add("status-dot-offline");
            }
        }
    }

    @FXML
    private void onPhotoClick() {
        if (currentRoomId.equals("dm_empty")) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Select Image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fc.showOpenDialog(photoButton.getScene().getWindow());
        if (file == null) return;

        inputField.setDisable(true);

        new Thread(() -> {
            try {
                String url = ApiClient.get().uploadImage(file, "chat");
                String baseUrl = Session.getApiBaseUrl();
                if (baseUrl.endsWith("/api")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 4);
                }
                String fullUrl = baseUrl + url;
                String imgMessage = "[img]" + fullUrl + "[/img]";

                if (chatClient.isConnected()) {
                    chatClient.send(imgMessage, currentRoomId);
                } else {
                    ApiClient.get().postJson("/chat/messages", Map.of("content", imgMessage, "roomId", currentRoomId));
                    Platform.runLater(() -> {
                        String myName = Session.getCurrentUser() != null ? Session.getCurrentUser().getDisplayName() : "Me";
                        LocalDateTime now = LocalDateTime.now();
                        ChatMessage msg = new ChatMessage(null, myName, imgMessage, true, now.format(TIME_FMT));
                        addMessageToList(msg, now, true);
                    });
                }
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(safeMessage(ex)));
            } finally {
                Platform.runLater(() -> {
                    inputField.setDisable(false);
                    inputField.requestFocus();
                });
            }
        }).start();
    }

    @FXML
    private void onSend() {
        String text = inputField.getText() != null ? inputField.getText().trim() : "";
        if (text.isEmpty() || currentRoomId.equals("dm_empty")) return;
        inputField.setDisable(true);
        sendText(text);
    }

    @FXML
    private void onHeartClick() {
        if (currentRoomId.equals("dm_empty")) return;
        sendText("❤️");
    }
    
    @FXML
    private void onEmojiToggleClick() {
        if (emojiPaletteBox != null) {
            boolean isVisible = emojiPaletteBox.isVisible();
            emojiPaletteBox.setVisible(!isVisible);
            emojiPaletteBox.setManaged(!isVisible);
        }
    }
    
    @FXML
    private void onInfoClick() {
        if (currentRoomId.equals("dm_empty")) return;
        String name = chatHeaderName.getText();
        UiHelper.showError("Contact Info: " + name + "\nActive since 2026.\nMore info coming soon!");
    }

    private void sendText(String text) {
        new Thread(() -> {
            try {
                if (chatClient.isConnected()) {
                    chatClient.send(text, currentRoomId);
                } else {
                    ApiClient.get().postJson("/chat/messages", Map.of("content", text, "roomId", currentRoomId));
                    Platform.runLater(() -> {
                        String myName = Session.getCurrentUser() != null ? Session.getCurrentUser().getDisplayName() : "Me";
                        LocalDateTime now = LocalDateTime.now();
                        ChatMessage msg = new ChatMessage(null, myName, text, true, now.format(TIME_FMT));
                        addMessageToList(msg, now, true);
                    });
                }
                Platform.runLater(() -> inputField.clear());
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(safeMessage(ex)));
            } finally {
                Platform.runLater(() -> {
                    inputField.setDisable(false);
                    inputField.requestFocus();
                });
            }
        }).start();
    }

    private void handleIncomingMessage(JsonNode msg) {
        String msgRoomId = msg.path("roomId").asText("");
        if (!msgRoomId.isEmpty() && !msgRoomId.equals(currentRoomId)) {
            return;
        }
        
        Long msgId = msg.path("id").isMissingNode() ? null : msg.path("id").asLong();
        if (msgId != null) {
            for (ChatMessage m : chatListView.getItems()) {
                if (m.id != null && m.id.equals(msgId)) {
                    String newReaction = msg.path("reaction").asText("");
                    m.reaction = newReaction.isEmpty() ? null : newReaction;
                    Platform.runLater(() -> chatListView.refresh());
                    return;
                }
            }
        }
        
        appendChatLine(msg, true);
    }

    private void appendChatLine(JsonNode msg, boolean scroll) {
        Long id = msg.path("id").isMissingNode() ? null : msg.path("id").asLong();
        String senderName = msg.path("senderName").asText("User");
        String content = msg.path("content").asText("");
        String reaction = msg.path("reaction").asText("");
        String myName = (Session.getCurrentUser() != null) ? Session.getCurrentUser().getDisplayName() : "Me";
        boolean mine = senderName.equals(myName);
        
        String timeStr = msg.path("sentAt").asText("");
        LocalDateTime dateTime = null;
        if (timeStr.isEmpty()) {
            dateTime = LocalDateTime.now();
        } else {
            try {
                dateTime = LocalDateTime.parse(timeStr);
            } catch (Exception e) {
                dateTime = LocalDateTime.now();
            }
        }
        String time = dateTime.format(TIME_FMT);

        ChatMessage message = new ChatMessage(id, mine ? "Me" : senderName, content, mine, time);
        message.reaction = reaction.isEmpty() ? null : reaction;
        
        LocalDateTime finalDateTime = dateTime;
        Platform.runLater(() -> {
            addMessageToList(message, finalDateTime, scroll);
        });
    }

    private void addMessageToList(ChatMessage message, LocalDateTime dateTime, boolean scroll) {
        if (dateTime != null) {
            LocalDate msgDate = dateTime.toLocalDate();
            if (lastMessageDate == null || !lastMessageDate.equals(msgDate)) {
                LocalDate today = LocalDate.now();
                String dateText;
                if (msgDate.equals(today)) {
                    dateText = "Today";
                } else if (msgDate.equals(today.minusDays(1))) {
                    dateText = "Yesterday";
                } else {
                    dateText = msgDate.format(DateTimeFormatter.ofPattern("MMM dd"));
                }
                chatListView.getItems().add(new ChatMessage(dateText));
                lastMessageDate = msgDate;
            }
        }
        chatListView.getItems().add(message);
        if (scroll) {
            chatListView.scrollTo(chatListView.getItems().size() - 1);
        }
    }

    private void updateTabStyles(Button activeBtn, Button inactiveBtn) {
        if (activeBtn != null && !activeBtn.getStyleClass().contains("seg-btn-active")) {
            activeBtn.getStyleClass().add("seg-btn-active");
        }
        if (inactiveBtn != null) {
            inactiveBtn.getStyleClass().remove("seg-btn-active");
        }
    }

    public void disconnect() {
        if (chatClient != null) {
            chatClient.disconnect();
        }
    }

    private String safeMessage(Exception e) { return e.getMessage() != null ? e.getMessage() : "Error"; }

    /** Simple immutable model for a single chat bubble. */
    private static class ChatMessage {
        final Long id;
        final String sender;
        final String content;
        final boolean mine;
        final String time;
        String reaction; // Visual only
        
        final boolean isDateHeader;
        final String dateText;

        ChatMessage(Long id, String sender, String content, boolean mine, String time) {
            this.id = id;
            this.sender = sender;
            this.content = content;
            this.mine = mine;
            this.time = time;
            this.isDateHeader = false;
            this.dateText = null;
        }

        ChatMessage(String dateText) {
            this.id = null;
            this.sender = null;
            this.content = null;
            this.mine = false;
            this.time = null;
            this.isDateHeader = true;
            this.dateText = dateText;
        }
    }

    private static StackPane avatarCircle(String name, String styleClass) {
        String initial = (name == null || name.isBlank()) ? "?" : name.trim().substring(0, 1).toUpperCase();
        StackPane circle = new StackPane();
        circle.getStyleClass().add(styleClass);
        circle.getChildren().add(new Label(initial));
        return circle;
    }

    private class ChatCell extends ListCell<ChatMessage> {
        @Override
        protected void updateItem(ChatMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            if (item.isDateHeader) {
                Label dateLabel = new Label(item.dateText);
                dateLabel.getStyleClass().add("chat-date-header");
                dateLabel.setStyle("-fx-background-color: #f1f2f6; -fx-padding: 4 12 4 12; -fx-background-radius: 12; -fx-text-fill: #747d8c; -fx-font-size: 11px; -fx-font-weight: bold;");
                
                HBox headerBox = new HBox(dateLabel);
                headerBox.setAlignment(Pos.CENTER);
                headerBox.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
                
                setGraphic(headerBox);
                setText(null);
                return;
            }

            boolean isImage = item.content.startsWith("[img]") && item.content.endsWith("[/img]");
            javafx.scene.Node messageNode;

            if (isImage) {
                String url = item.content.substring(5, item.content.length() - 6);
                ImageView imgView = new ImageView();
                imgView.setFitWidth(200);
                imgView.setPreserveRatio(true);
                ApiClient.get().loadImageAsync(url, imgView);

                VBox imgBox = new VBox(imgView);
                imgBox.getStyleClass().add(item.mine ? "bubble-outgoing" : "bubble-incoming");
                messageNode = imgBox;
            } else {
                javafx.scene.text.TextFlow flow = createEmojiTextFlow(item.content);
                VBox bubble = new VBox(flow);
                bubble.getStyleClass().add(item.mine ? "bubble-outgoing" : "bubble-incoming");
                messageNode = bubble;
            }
            
            // Wrap in StackPane to overlay reaction
            StackPane bubbleWrapper = new StackPane(messageNode);
            if (item.reaction != null) {
                ImageView reactionImg = new ImageView();
                reactionImg.setFitWidth(14);
                reactionImg.setFitHeight(14);
                ApiClient.get().loadImageAsync(getTwemojiUrl(item.reaction), reactionImg);
                
                StackPane reactionBubble = new StackPane(reactionImg);
                reactionBubble.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
                reactionBubble.getStyleClass().add("attached-reaction");
                StackPane.setAlignment(reactionBubble, item.mine ? Pos.BOTTOM_LEFT : Pos.BOTTOM_RIGHT);
                // Offset the reaction so it hangs off the bottom corner
                StackPane.setMargin(reactionBubble, new javafx.geometry.Insets(0, item.mine ? 0 : -8, -8, item.mine ? -8 : 0));
                bubbleWrapper.getChildren().add(reactionBubble);
            }

            Label timeLabel = new Label(item.time);
            timeLabel.getStyleClass().add("message-time-label");

            VBox bubbleColumn = new VBox(2);
            if (!item.mine) {
                Label senderLabel = new Label(item.sender);
                senderLabel.getStyleClass().add("message-sender-label");
                bubbleColumn.getChildren().add(senderLabel);
            }
            bubbleColumn.getChildren().add(bubbleWrapper);
            bubbleColumn.setAlignment(item.mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            HBox row = new HBox(8);
            row.setStyle("-fx-background-color: transparent;");

            // Reaction menu that shows on hover
            HBox reactionMenu = new HBox(4);
            reactionMenu.setAlignment(Pos.CENTER);
            reactionMenu.setMaxHeight(StackPane.USE_PREF_SIZE);
            reactionMenu.getStyleClass().add("reaction-menu");
            reactionMenu.setVisible(false);
            
            String[] reactionEmojis = {"👍", "❤️", "😂"};
            for (String emoji : reactionEmojis) {
                Button btn = new Button();
                btn.getStyleClass().add("reaction-btn");
                
                ImageView reactionImg = new ImageView();
                reactionImg.setFitWidth(18);
                reactionImg.setFitHeight(18);
                ApiClient.get().loadImageAsync(getTwemojiUrl(emoji), reactionImg);
                btn.setGraphic(reactionImg);
                
                btn.setOnAction(e -> {
                    item.reaction = emoji;
                    getListView().refresh();
                    if (item.id != null) {
                        new Thread(() -> {
                            try {
                                ApiClient.get().postJson("/chat/messages/" + item.id + "/react", Map.of("reaction", emoji));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }).start();
                    }
                });
                reactionMenu.getChildren().add(btn);
            }

            row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                reactionMenu.setVisible(isHovered);
            });

            if (item.mine) {
                row.setAlignment(Pos.CENTER_RIGHT);
                row.getChildren().addAll(reactionMenu, bubbleColumn, timeLabel);
            } else {
                StackPane avatar = avatarCircle(item.sender, "msg-avatar");
                row.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().addAll(avatar, bubbleColumn, reactionMenu, timeLabel);
            }

            setGraphic(row);
            setText(null);
        }
    }

    private class ContactCell extends ListCell<String> {
        @Override
        protected void updateItem(String name, boolean empty) {
            super.updateItem(name, empty);
            if (empty || name == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            StackPane avatar = avatarCircle(name, "contact-avatar");

            Label nameLabel = new Label(name);
            nameLabel.getStyleClass().add("contact-name");

            VBox textCol = new VBox(2);
            textCol.getChildren().add(nameLabel);

            HBox row = new HBox(10, avatar, textCol);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("contact-cell");
            HBox.setHgrow(textCol, Priority.ALWAYS);

            setGraphic(row);
            setText(null);
        }
    }
}