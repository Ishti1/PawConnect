package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MomentsController extends BaseListController {

    @FXML private VBox addForm;
    @FXML private TextField captionField;
    @FXML private TextField imageUrlField;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;

    private Long editingId;
    private String existingImageUrl;

    @FXML
    public void initialize() {
        loadData();
    }

    @FXML private void onAdd()       { showForm(true); }
    @FXML private void onRefresh()   { loadData(); }
    @FXML private void onCancelAdd() { showForm(false); }
    @FXML private void onChooseImage() { chooseImage(photoPreview, photoLabel); }
    @FXML private void onClearImage()  { clearSelectedImage(photoPreview, photoLabel, imageUrlField); }

    @FXML
    private void onSave() {
        boolean isEditing = editingId != null;
        if (!isEditing && !hasImageSelected(imageUrlField)) {
            com.catconnect.util.UiHelper.showError("Please choose a photo or paste an image URL.");
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("caption", captionField.getText().trim());
        File imageFile = selectedImageFile;
        String imageUrl = readImageUrlField(imageUrlField);
        if (isEditing && imageFile == null && imageUrl.isBlank() && existingImageUrl != null && !existingImageUrl.isBlank()) {
            imageUrl = existingImageUrl;
        }
        Long id = editingId;
        showForm(false);
        if (isEditing) {
            updateWithImage("/moments/" + id, body, imageFile, imageUrl);
        } else {
            postWithImage("/moments", body, imageFile, imageUrl);
        }
    }

    @Override protected String getApiPath()        { return "/moments"; }
    @Override protected String getDeletePath()     { return "/moments/"; }
    @Override protected String getUploadCategory() { return "moments"; }
    @Override
    protected VBox buildCard(JsonNode item) {
        long id = item.path("id").asLong();
        String caption = item.path("caption").asText("Cat moment");
        String posted = item.path("createdAt").asText("Unknown date");

        String detail = "📅 " + posted;
        return buildCatalogCard(item, id, caption, detail, "/moments/" + id + "/react", () -> beginEdit(item));
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong();
        existingImageUrl = imageUrlFrom(item);
        captionField.setText(item.path("caption").asText(""));
        imageUrlField.setText(existingImageUrl);
        addForm.setVisible(true);
        addForm.setManaged(true);
    }

    private void showForm(boolean show) {
        addForm.setVisible(show);
        addForm.setManaged(show);
        if (!show) {
            captionField.clear();
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null;
            existingImageUrl = null;
        }
    }
}
