package com.catconnect.controller;

import com.catconnect.service.ApiClient;
import com.catconnect.util.ImageUrlHelper;
import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.shape.Rectangle;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import javafx.stage.Modality;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * Base class for FXML-based list screens that support owner-only delete.
 * Subclasses provide the API path and override buildCard() to produce each row.
 */
public abstract class BaseListController {

    @FXML protected Label statusLabel;
    @FXML protected TilePane listBox;
    @FXML protected Label userLocationLabel;

    /** API endpoint to GET items (e.g. "/lost-found") */
    protected abstract String getApiPath();

    /** API endpoint prefix for DELETE (e.g. "/lost-found/") */
    protected abstract String getDeletePath();

    /** Refresh list from API without clearing the screen first. */
    protected void reload() {
        refreshListQuietly();
    }

    /** Build one card row for a single JSON item */
    protected abstract VBox buildCard(JsonNode item);

    /** Upload category passed to POST /api/upload (e.g. moments, lost-found) */
    protected abstract String getUploadCategory();

    protected File selectedImageFile;
    private boolean listLoadedOnce;

    // ── Loading ────────────────────────────────────────────────────

    /** First visit to a screen — show loading once. */
    protected void loadData() {
        loadData(!listLoadedOnce);
    }

    /** Refresh without wiping the list until new data arrives. */
    protected void refreshListQuietly() {
        loadData(false);
    }

    /** Use when switching filters (e.g. emergency vs all vets). */
    protected void resetListState() {
        listLoadedOnce = false;
    }

    /** Called by MainController when restoring a cached screen */
    public void refreshData() {
        resetListState();
        loadData();
    }

    private void loadData(boolean showLoading) {
        if (showLoading && statusLabel != null) {
            statusLabel.setText("Loading...");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        }

        if (userLocationLabel != null) {
            String selectedLoc = Session.getSelectedLocation();
            String locDisplay = (selectedLoc != null && !selectedLoc.isBlank()) ? selectedLoc : "All";
            Platform.runLater(() -> userLocationLabel.setText("\uD83D\uDCCD Location: " + locDisplay));
        }

        new Thread(() -> {
            try {
                JsonNode data = ApiClient.get().getList(getApiPath());
                Platform.runLater(() -> {
                    renderList(data);
                    listLoadedOnce = true;
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Error: " + e.getMessage());
                        statusLabel.setVisible(true);
                        statusLabel.setManaged(true);
                    }
                });
            }
        }).start();
    }

    private void renderList(JsonNode data) {
        if (statusLabel != null) {
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
        }
        if (listBox == null) return;
        
        if (listBox.getProperties().get("dynamicSetup") == null) {
            listBox.setAlignment(Pos.TOP_CENTER); // Align to the center
            listBox.getProperties().put("dynamicSetup", true);
        }
        
        listBox.getChildren().clear();

        if (!data.isArray() || data.isEmpty()) {
            Label empty = new Label("Nothing here yet. Be the first to add one!");
            empty.getStyleClass().add("empty-label");
            empty.setWrapText(true);
            if (statusLabel != null) {
                statusLabel.setText("Nothing here yet.");
                statusLabel.setVisible(true);
                statusLabel.setManaged(true);
            }
            return;
        }

        String selectedLocation = com.catconnect.util.Session.getSelectedLocation();
        boolean hasLocationFilter = selectedLocation != null && !selectedLocation.equals("All") && !selectedLocation.isBlank();
        String target = hasLocationFilter ? selectedLocation.toLowerCase() : "";

        for (JsonNode item : data) {
            boolean matches = true;
            if (hasLocationFilter) {
                matches = item.toString().toLowerCase().contains(target);
            }
            if (matches) {
                listBox.getChildren().add(wrapCard(item));
            }
        }
        
        renderListHook(data);
    }
    
    protected void renderListHook(JsonNode data) {
        // Subclasses can override this
    }

    private VBox wrapCard(JsonNode item) {
        VBox card = buildCard(item);
        long id = item.path("id").asLong(-1);
        if (id >= 0) {
            card.setId(cardId(id));
        }
        return card;
    }

    private static String cardId(long id) {
        return "item-" + id;
    }

    protected void upsertItemInList(JsonNode item) {
        if (listBox == null || item == null || !item.has("id")) {
            refreshListQuietly();
            return;
        }
        hideEmptyState();
        long id = item.path("id").asLong();
        VBox card = wrapCard(item);
        var children = listBox.getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (cardId(id).equals(children.get(i).getId())) {
                children.set(i, card);
                return;
            }
        }
        listBox.getChildren().add(0, card);
    }

    protected void removeItemFromList(long id) {
        if (listBox == null) {
            return;
        }
        listBox.getChildren().removeIf(n -> cardId(id).equals(n.getId()));
        if (listBox.getChildren().isEmpty()) {
            showEmptyState();
        }
    }

    private void hideEmptyState() {
        if (statusLabel != null) {
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
        }
    }

    private void showEmptyState() {
        if (statusLabel != null) {
            statusLabel.setText("Nothing here yet.");
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        }
    }

    // ── Ownership helpers ──────────────────────────────────────────

    protected boolean isOwn(JsonNode item) {
        if (!item.hasNonNull("userId")) return false;
        var user = Session.getCurrentUser();
        if (user == null || user.getId() == null) return false;
        return item.path("userId").asLong() == user.getId();
    }

    protected Button deleteButton(long id) {
        Button btn = new Button("Delete");
        btn.getStyleClass().add("delete-button");
        btn.setOnAction(e -> confirmDelete(id));
        return btn;
    }

    protected void confirmDelete(long id) {
        if (!Session.isLoggedIn()) {
            UiHelper.showError("You must be logged in.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete");
        alert.setHeaderText(null);
        alert.setContentText("Remove this item?");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        ApiClient.get().delete(getDeletePath() + id);
                        Platform.runLater(() -> {
                            UiHelper.showInfo("Deleted.");
                            removeItemFromList(id);
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
                    }
                }).start();
            }
        });
    }

    // ── Card builder helpers (usable by subclasses) ────────────────

    protected VBox makeCardBox(String title, String detail) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        t.setWrapText(true);

        Label d = new Label(detail);
        d.getStyleClass().add("card-detail");
        d.setWrapText(true);

        card.getChildren().addAll(t, d);
        return card;
    }

    protected HBox cardHeader(String title, Button... extraButtons) {
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        t.setWrapText(true);
        HBox.setHgrow(t, Priority.ALWAYS);

        HBox header = new HBox(12, t);
        header.setAlignment(Pos.CENTER_LEFT);
        for (Button b : extraButtons) {
            header.getChildren().add(b);
        }
        return header;
    }

    protected void postAndReload(String path, java.util.Map<String, Object> body) {
        new Thread(() -> {
            try {
                JsonNode created = ApiClient.get().postJson(path, body);
                Platform.runLater(() -> upsertItemInList(created));
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
            }
        }).start();
    }

    /**
     * Upload image then POST. Pass file/url captured before clearing the form —
     * showForm(false) clears selectedImageFile and URL fields immediately.
     */
    protected void postWithImage(String path, java.util.Map<String, Object> body,
                                 File imageFile, String optionalImageUrl) {
        new Thread(() -> {
            try {
                if (imageFile != null) {
                    body.put("imageUrl", ApiClient.get().uploadImage(imageFile, getUploadCategory()));
                } else if (optionalImageUrl != null && !optionalImageUrl.isBlank()) {
                    body.put("imageUrl", optionalImageUrl.trim());
                }
                JsonNode created = ApiClient.get().postJson(path, body);
                Platform.runLater(() -> upsertItemInList(created));
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
            }
        }).start();
    }

    protected void updateWithImage(String path, java.util.Map<String, Object> body,
                                   File imageFile, String optionalImageUrl) {
        new Thread(() -> {
            try {
                if (imageFile != null) {
                    body.put("imageUrl", ApiClient.get().uploadImage(imageFile, getUploadCategory()));
                } else if (optionalImageUrl != null && !optionalImageUrl.isBlank()) {
                    body.put("imageUrl", optionalImageUrl.trim());
                }
                JsonNode updated = ApiClient.get().putJson(path, body);
                Platform.runLater(() -> upsertItemInList(updated));
            } catch (Exception ex) {
                Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
            }
        }).start();
    }

    protected String readImageUrlField(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }

    // ── Image picker (add forms) ───────────────────────────────────

    protected void chooseImage(ImageView preview, Label photoLabel) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a photo");

        // Corrected line: strictly allows native JavaFX formats, avoiding WebP loading errors
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = listBox != null && listBox.getScene() != null
                ? (Stage) listBox.getScene().getWindow()
                : null;
        if (stage == null) {
            UiHelper.showError("Could not open file chooser.");
            return;
        }
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            openCropModal(file, preview, photoLabel, stage);
        }
    }

    private double cropDragStartX, cropDragStartY;

    private void openCropModal(File file, ImageView preview, Label photoLabel, Stage owner) {
        Stage cropStage = new Stage();
        cropStage.initModality(Modality.APPLICATION_MODAL);
        cropStage.initOwner(owner);
        cropStage.setTitle("Crop Image");

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #222222; -fx-padding: 30;");

        Label instructions = new Label("Drag to position image");
        instructions.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        StackPane cropContainer = new StackPane();
        cropContainer.setStyle("-fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2;");

        // Use a generic 4:3 crop ratio since these are usually landscape cards
        double cropWidth = 400;
        double cropHeight = 300;
        cropContainer.setMinSize(cropWidth, cropHeight);
        cropContainer.setMaxSize(cropWidth, cropHeight);

        Image img = new Image(file.toURI().toString());
        ImageView cropImageView = new ImageView(img);
        cropImageView.setPreserveRatio(true);

        Pane pane = new Pane(cropImageView);
        pane.setMinSize(cropWidth, cropHeight);
        pane.setMaxSize(cropWidth, cropHeight);
        
        Rectangle clip = new Rectangle(cropWidth, cropHeight);
        pane.setClip(clip);

        // Fit image inside pane nicely initially to cover it
        if (img.getWidth() / img.getHeight() > cropWidth / cropHeight) {
            cropImageView.setFitHeight(cropHeight);
        } else {
            cropImageView.setFitWidth(cropWidth);
        }

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

        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> cropStage.close());
        
        Button doneBtn = new Button("Done");
        doneBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        doneBtn.setOnAction(e -> {
            WritableImage snapshot = pane.snapshot(new SnapshotParameters(), null);
            try {
                File tempFile = File.createTempFile("paw_listcrop_", ".png");
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", tempFile);
                
                selectedImageFile = tempFile;
                if (preview != null) {
                    preview.setImage(new Image(tempFile.toURI().toString(), 220, 220, true, true));
                }
                if (photoLabel != null) {
                    photoLabel.setText("Selected: Cropped Image");
                }
                cropStage.close();
            } catch (Exception ex) {
                UiHelper.showError("Failed to save cropped image.");
            }
        });
        
        buttons.getChildren().addAll(cancelBtn, doneBtn);
        root.getChildren().addAll(instructions, cropContainer, buttons);
        
        Scene scene = new Scene(root);
        cropStage.setScene(scene);
        cropStage.showAndWait();
    }

    protected void clearSelectedImage(ImageView preview, Label photoLabel, TextField optionalUrlField) {
        selectedImageFile = null;
        if (preview != null) {
            preview.setImage(null);
        }
        if (photoLabel != null) {
            photoLabel.setText("No photo selected");
        }
        if (optionalUrlField != null) {
            optionalUrlField.clear();
        }
    }

    protected boolean hasImageSelected(TextField optionalUrlField) {
        if (selectedImageFile != null) {
            return true;
        }
        return optionalUrlField != null
                && optionalUrlField.getText() != null
                && !optionalUrlField.getText().isBlank();
    }

    protected String imageUrlFrom(JsonNode item) {
        if (!item.has("imageUrl") || item.get("imageUrl").isNull()) {
            return "";
        }
        return item.get("imageUrl").asText("").trim();
    }

    protected ImageView createCardImageView(String imageUrl) {
        ImageView view = new ImageView();
        view.setFitHeight(140);
        view.setPreserveRatio(true);
        view.getStyleClass().add("card-image");
        String resolved = ImageUrlHelper.resolve(imageUrl);
        if (resolved != null) {
            ApiClient.get().loadImageAsync(resolved, view);
            view.setOnMouseClicked(e -> openImageFullscreen(resolved));
        }
        return view;
    }

    protected void openImageFullscreen(String imageUrl) {
        Stage stage = new Stage();
        stage.setTitle("Photo");

        ImageView iv = new ImageView();
        iv.setPreserveRatio(true);

        BorderPane root = new BorderPane(iv);
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, 900, 650);
        iv.fitWidthProperty().bind(scene.widthProperty());
        iv.fitHeightProperty().bind(scene.heightProperty());

        stage.setScene(scene);
        stage.show();

        ApiClient.get().loadImageAsync(imageUrl, iv);
    }

    protected void insertCardImage(VBox card, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        int index = Math.min(1, card.getChildren().size());
        card.getChildren().add(index, createCardImageView(imageUrl));
    }

    // ── Reactions (👍) ─────────────────────────────────────────────

    protected VBox buildCatalogCard(JsonNode item, long id, String title, String detail,
                                    String reactPath, Runnable onEdit) {

        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        if (listBox != null) {
            card.prefWidthProperty().bind(listBox.prefTileWidthProperty());
            card.maxWidthProperty().bind(listBox.prefTileWidthProperty());
        } else {
            card.setPrefWidth(480);
            card.setMaxWidth(480);
        }
        card.setFillWidth(true);

        String img = imageUrlFrom(item);

        if (img != null && !img.isBlank()) {
            ImageView imageView = createCardImageView(img);
            imageView.setFitHeight(220);
            if (listBox != null) {
                imageView.fitWidthProperty().bind(listBox.prefTileWidthProperty().subtract(36));
            } else {
                imageView.setFitWidth(444);
            }
            imageView.setPreserveRatio(false);
            
            // Hide if image fails to load to prevent huge white space
            imageView.managedProperty().bind(imageView.imageProperty().isNotNull());
            imageView.visibleProperty().bind(imageView.imageProperty().isNotNull());

            card.getChildren().add(imageView);
        }

        VBox infoBox = new VBox(10);
        infoBox.setMaxWidth(Double.MAX_VALUE);

        HBox header;
        if (isOwn(item)) {
            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("edit-button");
            editBtn.setOnAction(e -> onEdit.run());
            header = cardHeader(title, editBtn, deleteButton(id));
        } else {
            header = cardHeader(title);
        }

        Label detailLbl = new Label(detail);
        detailLbl.getStyleClass().add("card-detail");
        detailLbl.setWrapText(true);
        detailLbl.setMaxWidth(Double.MAX_VALUE);



        infoBox.getChildren().addAll(header, detailLbl);
        card.getChildren().add(infoBox);

        return card;
    }

    protected Button addressLinkButton(String mapLink) {
        if (mapLink == null || mapLink.isBlank()) {
            return null;
        }
        Button btn = new Button("Address Link");
        btn.getStyleClass().add("secondary-button");
        String url = mapLink.trim();
        btn.setOnAction(e -> openAddressLink(url));
        return btn;
    }

    protected void openAddressLink(String mapLink) {
        try {
            URI uri = URI.create(mapLink.trim());
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(uri);
            } else {
                UiHelper.showError("Desktop browser is not supported.");
            }
        } catch (Exception ex) {
            UiHelper.showError("Could not open address link. Paste a full URL (https://...).");
        }
    }

    protected HBox reactionBar(String reactPath, int likes, Runnable afterReact) {
        Label likesLabel = new Label(String.valueOf(likes));
        likesLabel.getStyleClass().add("card-detail");

        Button loveBtn = new Button("♥");
        loveBtn.getStyleClass().add("love-button");
        loveBtn.setOnAction(e -> {
            loveBtn.setDisable(true);
            new Thread(() -> {
                try {
                    JsonNode updated = ApiClient.get().postJson(reactPath, Map.of());
                    int newLikes = updated.path("likes").asInt(likes + 1);
                    Platform.runLater(() -> likesLabel.setText(String.valueOf(newLikes)));
                    if (afterReact != null) {
                        Platform.runLater(afterReact);
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> UiHelper.showError(ex.getMessage()));
                } finally {
                    Platform.runLater(() -> loveBtn.setDisable(false));
                }
            }).start();
        });

        HBox bar = new HBox(6, loveBtn, likesLabel);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }
}