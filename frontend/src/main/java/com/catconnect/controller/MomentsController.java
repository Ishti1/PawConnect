package com.catconnect.controller;

import com.catconnect.service.ApiClient;
import com.catconnect.util.ImageUrlHelper;
import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MomentsController {

    @FXML private ListView<JsonNode> feedListView;
    @FXML private Label statusLabel;
    
    // Add Form Modal
    @FXML private StackPane addFormOverlay;
    @FXML private TextField captionField;
    @FXML private TextField mediaUrlField;
    @FXML private Label mediaLabel;

    // Comments Modal
    @FXML private StackPane commentsOverlay;
    @FXML private ListView<JsonNode> commentsListView;
    @FXML private TextField commentField;

    // Crop Modal
    @FXML private StackPane cropOverlay;
    @FXML private StackPane cropContainer;

    private File selectedMediaFile;
    private File uncroppedMediaFile;
    private ImageView cropImageView;
    private double cropDragStartX, cropDragStartY;

    private Long activeMomentIdForComments;

    // PawConnect color constants
    private static final String PRIMARY_COLOR = "#ff6b6b";
    private static final String DARK_BG = "#1a1a2e";
    private static final String CARD_BG = "#16213e";
    
    // Performance: Shared HTTP client, Thread pool, and Image cache
    private static final java.net.http.HttpClient sharedHttpClient = java.net.http.HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL).build();
    private static final java.util.concurrent.ExecutorService imageExecutor = java.util.concurrent.Executors.newFixedThreadPool(4);
    private static final java.util.concurrent.ConcurrentHashMap<String, javafx.scene.image.Image> imageCache = new java.util.concurrent.ConcurrentHashMap<>();

    @FXML
    public void initialize() {
        feedListView.setCellFactory(param -> new MomentCell());
        feedListView.setStyle("-fx-background-color: #ff6b6b;");
        
        commentsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(JsonNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(8, 12, 8, 12));
                    
                    // Avatar circle
                    String initial = item.path("username").asText("U").substring(0, 1).toUpperCase();
                    Label avatar = new Label(initial);
                    avatar.setMinSize(30, 30);
                    avatar.setMaxSize(30, 30);
                    avatar.setAlignment(Pos.CENTER);
                    avatar.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-background-radius: 15; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                    
                    VBox textBox = new VBox(2);
                    Label name = new Label(item.path("username").asText("Unknown"));
                    name.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px;");
                    Label content = new Label(item.path("content").asText(""));
                    content.setWrapText(true);
                    content.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 13px;");
                    textBox.getChildren().addAll(name, content);
                    HBox.setHgrow(textBox, Priority.ALWAYS);
                    
                    row.getChildren().addAll(avatar, textBox);
                    setGraphic(row);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
        
        loadData();
    }

    private void loadData() {
        statusLabel.setText("Loading posts...");
        statusLabel.setVisible(true);
        new Thread(() -> {
            try {
                JsonNode data = ApiClient.get().getList("/moments");
                Platform.runLater(() -> {
                    statusLabel.setVisible(false);
                    feedListView.getItems().clear();
                    if (data.isArray()) {
                        for (JsonNode node : data) {
                            feedListView.getItems().add(node);
                        }
                    }
                    if (feedListView.getItems().isEmpty()) {
                        statusLabel.setText("No posts yet. Be the first to post!");
                        statusLabel.setVisible(true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML private void onAdd() { 
        addFormOverlay.setVisible(true); 
        addFormOverlay.setManaged(true); 
    }
    
    @FXML private void onRefresh() { loadData(); }
    
    @FXML private void onCancelAdd() { 
        addFormOverlay.setVisible(false); 
        addFormOverlay.setManaged(false); 
        captionField.clear();
        mediaUrlField.clear();
        selectedMediaFile = null;
        mediaLabel.setText("No file selected");
    }

    @FXML private void onChooseMedia() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Media");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Media Files", "*.png", "*.jpg", "*.jpeg", "*.mp4"));
        Stage stage = (Stage) feedListView.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                uncroppedMediaFile = file;
                openCropOverlay(file);
            } else {
                selectedMediaFile = file;
                mediaLabel.setText("Selected: " + file.getName());
            }
        }
    }

    private void openCropOverlay(File file) {
        cropOverlay.setVisible(true);
        cropOverlay.setManaged(true);
        cropContainer.getChildren().clear();

        Image img = new Image(file.toURI().toString());
        cropImageView = new ImageView(img);
        cropImageView.setPreserveRatio(true);
        
        // Setup crop container size (Instagram 9:16 approx)
        double cropWidth = 360;
        double cropHeight = 640;
        cropContainer.setMinSize(cropWidth, cropHeight);
        cropContainer.setMaxSize(cropWidth, cropHeight);

        Pane pane = new Pane(cropImageView);
        pane.setMinSize(cropWidth, cropHeight);
        pane.setMaxSize(cropWidth, cropHeight);
        
        Rectangle clip = new Rectangle(cropWidth, cropHeight);
        pane.setClip(clip);

        // Fit image inside pane nicely initially
        if (img.getWidth() / img.getHeight() > cropWidth / cropHeight) {
            cropImageView.setFitHeight(cropHeight);
        } else {
            cropImageView.setFitWidth(cropWidth);
        }

        // Add dragging logic
        cropImageView.setOnMousePressed(e -> {
            cropDragStartX = e.getSceneX();
            cropDragStartY = e.getSceneY();
        });
        
        cropImageView.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - cropDragStartX;
            double dy = e.getSceneY() - cropDragStartY;
            cropImageView.setTranslateX(cropImageView.getTranslateX() + dx);
            cropImageView.setTranslateY(cropImageView.getTranslateY() + dy);
            cropDragStartX = e.getSceneX();
            cropDragStartY = e.getSceneY();
        });

        cropContainer.getChildren().add(pane);
    }

    @FXML private void onCancelCrop() {
        cropOverlay.setVisible(false);
        cropOverlay.setManaged(false);
        uncroppedMediaFile = null;
    }

    @FXML private void onConfirmCrop() {
        if (cropContainer.getChildren().isEmpty()) return;
        Pane pane = (Pane) cropContainer.getChildren().get(0);
        
        WritableImage snapshot = pane.snapshot(new SnapshotParameters(), null);
        try {
            File tempFile = File.createTempFile("paw_crop_", ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", tempFile);
            selectedMediaFile = tempFile;
            mediaLabel.setText("Selected: Cropped Image");
        } catch (Exception e) {
            UiHelper.showError("Failed to save cropped image.");
        }
        
        cropOverlay.setVisible(false);
        cropOverlay.setManaged(false);
    }

    @FXML private void onClearMedia() {
        selectedMediaFile = null;
        mediaLabel.setText("No file selected");
        mediaUrlField.clear();
    }

    @FXML private void onSave() {
        if (selectedMediaFile == null && mediaUrlField.getText().isBlank()) {
            UiHelper.showError("Please choose a file or paste a URL.");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("caption", captionField.getText().trim());
        
        String url = mediaUrlField.getText().trim();
        String mediaType = "IMAGE";
        
        File fileToUpload = selectedMediaFile; // Capture before clearing
        
        if (fileToUpload != null) {
            if (fileToUpload.getName().toLowerCase().endsWith(".mp4")) {
                mediaType = "VIDEO";
            }
        } else if (url.toLowerCase().endsWith(".mp4")) {
            mediaType = "VIDEO";
        }
        
        body.put("mediaType", mediaType);
        onCancelAdd();
        
        new Thread(() -> {
            try {
                if (fileToUpload != null) {
                    body.put("imageUrl", ApiClient.get().uploadImage(fileToUpload, "moments"));
                } else {
                    body.put("imageUrl", url);
                }
                ApiClient.get().postJson("/moments", body);
                Platform.runLater(this::loadData);
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
            }
        }).start();
    }

    @FXML private void onCloseComments() {
        commentsOverlay.setVisible(false);
        commentsOverlay.setManaged(false);
        activeMomentIdForComments = null;
    }

    @FXML private void onPostComment() {
        String text = commentField.getText().trim();
        if (text.isEmpty() || activeMomentIdForComments == null) return;
        
        long momentId = activeMomentIdForComments;
        new Thread(() -> {
            try {
                ApiClient.get().postJson("/moments/" + momentId + "/comments", Map.of("content", text));
                Platform.runLater(() -> {
                    commentField.clear();
                    openComments(momentId);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
            }
        }).start();
    }

    private void openComments(long momentId) {
        activeMomentIdForComments = momentId;
        commentsOverlay.setVisible(true);
        commentsOverlay.setManaged(true);
        commentsListView.getItems().clear();
        
        new Thread(() -> {
            try {
                JsonNode data = ApiClient.get().getList("/moments/" + momentId + "/comments");
                Platform.runLater(() -> {
                    if (data.isArray()) {
                        for (JsonNode node : data) {
                            commentsListView.getItems().add(node);
                        }
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError("Failed to load comments"));
            }
        }).start();
    }

    private void shareMoment(long id) {
        new Thread(() -> {
            try {
                ApiClient.get().postJson("/moments/" + id + "/share", Map.of());
                Platform.runLater(() -> {
                    UiHelper.showInfo("Post shared to your feed!");
                    loadData();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
            }
        }).start();
    }

    private void deleteMoment(long id, JsonNode item) {
        new Thread(() -> {
            try {
                ApiClient.get().delete("/moments/" + id);
                Platform.runLater(() -> feedListView.getItems().remove(item));
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
            }
        }).start();
    }

    private void likeMoment(long id, Label likesLabel) {
        // Optimistic UI update
        try {
            int current = Integer.parseInt(likesLabel.getText());
            likesLabel.setText(String.valueOf(current + 1));
        } catch (NumberFormatException ignored) {}

        new Thread(() -> {
            try {
                ApiClient.get().postJson("/moments/" + id + "/react", Map.of());
                // No need to reload data, UI is already updated
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
            }
        }).start();
    }

    /**
     * Full-screen Instagram Reels style cell.
     * 
     * Each post takes the FULL height/width of the feed area.
     * Image fills the entire cell as background.
     * Username + caption overlay at bottom-left.
     * Action buttons (heart, comment, share) overlay at bottom-right.
     */
    private class MomentCell extends ListCell<JsonNode> {
        private final HBox root;
        private final StackPane mediaContainer;
        private MediaPlayer mediaPlayer;
        private MediaView mediaView;
        private final Pane imagePane;
        private final Label captionLabel;
        private final Label usernameLabel;
        private final Label likesLabel;
        private final Label noImageLabel;
        private final Button likeBtn;
        private final Button commentBtn;
        private final Button shareBtn;
        private final Button deleteBtn;
        
        public MomentCell() {
            // Desktop Instagram layout: Centered media container, actions on the right
            root = new HBox(16); // Spacing between media and actions
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: transparent; -fx-padding: 20 0;");
            
            mediaContainer = new StackPane();
            mediaContainer.setStyle("-fx-background-color: #000000; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
            
            // Clip for rounded corners
            Rectangle clip = new Rectangle();
            clip.setArcWidth(24);
            clip.setArcHeight(24);
            mediaContainer.setClip(clip);
            
            // --- Image (fills entire cell using cover) ---
            imagePane = new Pane();
            
            // --- Video ---
            mediaView = new MediaView();
            mediaView.setPreserveRatio(true);
            
            // --- "No image" placeholder ---
            noImageLabel = new Label("🐱");
            noImageLabel.setStyle("-fx-font-size: 80px; -fx-text-fill: #333333;");
            noImageLabel.setAlignment(Pos.CENTER);
            StackPane.setAlignment(noImageLabel, Pos.CENTER);
            
            // --- Bottom-left overlay: username + caption ---
            VBox bottomLeft = new VBox(4);
            bottomLeft.setAlignment(Pos.BOTTOM_LEFT);
            bottomLeft.setPickOnBounds(false);
            bottomLeft.setPadding(new Insets(0, 16, 20, 16));
            bottomLeft.setMaxHeight(Region.USE_PREF_SIZE);
            
            usernameLabel = new Label();
            usernameLabel.setStyle(
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 6, 0.6, 0, 1);");
            
            captionLabel = new Label();
            captionLabel.setStyle(
                "-fx-text-fill: #eeeeee; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 6, 0.6, 0, 1);");
            captionLabel.setWrapText(true);
            captionLabel.setMaxWidth(350);
            
            // Profile bubble and username
            HBox userBox = new HBox(8);
            userBox.setAlignment(Pos.CENTER_LEFT);
            javafx.scene.shape.Circle profileBubble = new javafx.scene.shape.Circle(14, javafx.scene.paint.Color.WHITE);
            profileBubble.setStroke(javafx.scene.paint.Color.web("#eeeeee"));
            profileBubble.setStrokeWidth(1.5);
            // Optional: add a tiny cat icon or first letter inside the bubble
            // For now, just a white circle bubble
            userBox.getChildren().addAll(profileBubble, usernameLabel);
            
            bottomLeft.getChildren().addAll(userBox, captionLabel);
            StackPane.setAlignment(bottomLeft, Pos.BOTTOM_LEFT);
            
            // Add elements to media container
            mediaContainer.getChildren().addAll(imagePane, mediaView, noImageLabel, bottomLeft);
            
            // --- Right side actions: Like, Comment, Share, Delete ---
            VBox actionsColumn = new VBox(22);
            actionsColumn.setAlignment(Pos.BOTTOM_CENTER);
            actionsColumn.setPadding(new Insets(0, 0, 20, 0));
            actionsColumn.setPickOnBounds(false);
            actionsColumn.setMaxWidth(Region.USE_PREF_SIZE);
            
            likeBtn = new Button("♥");
            likeBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #ff6b6b; -fx-font-size: 22px; -fx-cursor: hand; -fx-background-radius: 50; -fx-min-width: 44; -fx-min-height: 44; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            likesLabel = new Label("0");
            likesLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px; -fx-font-weight: bold;");
            VBox likeBox = new VBox(4, likeBtn, likesLabel);
            likeBox.setAlignment(Pos.CENTER);
            
            commentBtn = new Button("💬");
            commentBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-font-size: 20px; -fx-cursor: hand; -fx-background-radius: 50; -fx-min-width: 44; -fx-min-height: 44; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            shareBtn = new Button("➤");
            shareBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-font-size: 20px; -fx-cursor: hand; -fx-background-radius: 50; -fx-min-width: 44; -fx-min-height: 44; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: " + PRIMARY_COLOR + "; -fx-font-size: 20px; -fx-cursor: hand; -fx-background-radius: 50; -fx-min-width: 44; -fx-min-height: 44; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            actionsColumn.getChildren().addAll(likeBox, commentBtn, shareBtn, deleteBtn);
            
            // Add container and actions to root HBox
            root.getChildren().addAll(mediaContainer, actionsColumn);
        }
        
        @Override
        protected void updateItem(JsonNode item, boolean empty) {
            super.updateItem(item, empty);
            
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
            
            if (empty || item == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            } else {
                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                
                long id = item.path("id").asLong();
                String mediaType = item.path("mediaType").asText("IMAGE");
                String url = item.path("imageUrl").asText("");
                String caption = item.path("caption").asText("");
                String username = item.path("username").asText("Unknown");
                int likes = item.path("likes").asInt(0);
                
                if (item.has("sharedMoment") && !item.path("sharedMoment").isNull()) {
                    JsonNode shared = item.path("sharedMoment");
                    username = username + " 🔁 " + shared.path("username").asText("");
                    mediaType = shared.path("mediaType").asText("IMAGE");
                    url = shared.path("imageUrl").asText("");
                }
                
                usernameLabel.setText(username);
                captionLabel.setText(caption);
                likesLabel.setText(String.valueOf(likes));
                
                // Container dimensions
                double cellW = feedListView.getWidth();
                double cellH = feedListView.getHeight();
                
                // On very first load, getHeight might be 0, so fallback to a reasonable default
                if (cellH < 100) cellH = 600;
                
                root.setMinHeight(cellH);
                root.setPrefHeight(cellH);
                root.setMaxHeight(cellH);
                
                // Allow width to fit naturally, but center elements
                root.setMaxWidth(Double.MAX_VALUE);
                
                // Instagram desktop Reels ratio is about 9:16, max width ~ 350-400
                double mediaHeight = cellH - 40; // 20px padding top/bottom
                double mediaWidth = Math.min(mediaHeight * 9.0 / 16.0, 420);
                
                mediaContainer.setMinSize(mediaWidth, mediaHeight);
                mediaContainer.setPrefSize(mediaWidth, mediaHeight);
                mediaContainer.setMaxSize(mediaWidth, mediaHeight);
                
                ((Rectangle) mediaContainer.getClip()).setWidth(mediaWidth);
                ((Rectangle) mediaContainer.getClip()).setHeight(mediaHeight);
                
                String resolvedUrl = ImageUrlHelper.resolve(url);
                boolean hasMedia = resolvedUrl != null && !resolvedUrl.isEmpty() 
                                   && !resolvedUrl.equals(ImageUrlHelper.resolve(""));
                
                if ("VIDEO".equalsIgnoreCase(mediaType) && hasMedia) {
                    imagePane.setVisible(false);
                    imagePane.setManaged(false);
                    mediaView.setVisible(true);
                    mediaView.setManaged(true);
                    noImageLabel.setVisible(false);
                    
                    try {
                        Media media = new Media(resolvedUrl);
                        mediaPlayer = new MediaPlayer(media);
                        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                        mediaView.setMediaPlayer(mediaPlayer);
                        mediaView.setFitWidth(mediaWidth);
                        mediaView.setFitHeight(mediaHeight);
                        mediaPlayer.play();
                    } catch (Exception e) {
                        System.err.println("Video error: " + e.getMessage());
                        noImageLabel.setVisible(true);
                    }
                } else if (hasMedia) {
                    mediaView.setVisible(false);
                    mediaView.setManaged(false);
                    imagePane.setVisible(true);
                    imagePane.setManaged(true);
                    noImageLabel.setVisible(false);
                    
                    // Load image securely using robust HTTP client to bypass CDNs blocking Java
                    loadImageSafely(resolvedUrl, imagePane);
                } else {
                    // No image URL — show placeholder
                    imagePane.setVisible(false);
                    imagePane.setManaged(false);
                    mediaView.setVisible(false);
                    mediaView.setManaged(false);
                    noImageLabel.setVisible(true);
                    mediaContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #fff0f5, #ffe4e1);");
                }
                
                // Wire actions
                likeBtn.setOnAction(e -> likeMoment(id, likesLabel));
                commentBtn.setOnAction(e -> openComments(id));
                shareBtn.setOnAction(e -> shareMoment(id));
                
                long userId = item.path("userId").asLong();
                com.catconnect.model.User currentUser = Session.getCurrentUser();
                if (currentUser != null && currentUser.getId() == userId) {
                    deleteBtn.setVisible(true);
                    deleteBtn.setManaged(true);
                    deleteBtn.setOnAction(e -> deleteMoment(id, item));
                } else {
                    deleteBtn.setVisible(false);
                    deleteBtn.setManaged(false);
                }
                
                setGraphic(root);
            }
        }
        
        private void loadImageSafely(String url, Pane target) {
            if (imageCache.containsKey(url)) {
                Image img = imageCache.get(url);
                BackgroundImage bgImg = new BackgroundImage(
                    img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
                );
                target.setBackground(new Background(bgImg));
                return;
            }
            
            imageExecutor.submit(() -> {
                try {
                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create(url))
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                            .header("Accept", "image/*")
                            .timeout(java.time.Duration.ofSeconds(15))
                            .GET()
                            .build();
                    java.net.http.HttpResponse<byte[]> response = sharedHttpClient
                            .send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
                    
                    if (response.statusCode() < 400 && response.body() != null && response.body().length > 0) {
                        try {
                            Image img = new Image(new java.io.ByteArrayInputStream(response.body()));
                            imageCache.put(url, img);
                            Platform.runLater(() -> {
                                BackgroundImage bgImg = new BackgroundImage(
                                    img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
                                );
                                target.setBackground(new Background(bgImg));
                            });
                        } catch (Exception e) {
                            System.err.println("Failed to construct image from bytes: " + e.getMessage());
                        }
                    } else {
                        System.err.println("HTTP " + response.statusCode() + " when loading image: " + url);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load image " + url + ": " + e.getMessage());
                }
            });
        }
    }
}
