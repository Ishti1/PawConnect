package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SheltersController extends BaseListController {

    @FXML private VBox addForm;
    @FXML private TextField nameField, addressField, phoneField, emailField, capacityField, cityField, imageUrlField;
    @FXML private TextArea descField;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;
    private Long editingId;
    private String existingImageUrl;

    @FXML public void initialize() { loadData(); }
    @FXML private void onAdd() { showForm(true); }
    @FXML private void onRefresh() { loadData(); }
    @FXML private void onCancelAdd() { showForm(false); }
    @FXML private void onChooseImage() { chooseImage(photoPreview, photoLabel); }
    @FXML private void onClearImage() { clearSelectedImage(photoPreview, photoLabel, imageUrlField); }

    @FXML private void onSave() {
        if (nameField.getText().isBlank()) { com.catconnect.util.UiHelper.showError("Name is required."); return; }
        Map<String, Object> body = new HashMap<>();
        body.put("name", nameField.getText().trim());
        body.put("address", addressField.getText().trim());
        body.put("phone", phoneField.getText().trim());
        body.put("email", emailField.getText().trim());
        body.put("description", descField.getText().trim());
        body.put("city", cityField.getText().trim());
        try { body.put("capacity", Integer.parseInt(capacityField.getText().trim())); } catch (NumberFormatException ignored) {}
        File img = selectedImageFile; String url = readImageUrlField(imageUrlField);
        if (editingId != null && img == null && url.isBlank() && existingImageUrl != null) url = existingImageUrl;
        Long id = editingId; showForm(false);
        if (id != null) updateWithImage("/shelters/" + id, body, img, url);
        else postWithImage("/shelters", body, img, url);
    }

    @Override protected String getApiPath() { return "/shelters"; }
    @Override protected String getDeletePath() { return "/shelters/"; }
    @Override protected String getUploadCategory() { return "shelters"; }
    @Override protected VBox buildCard(JsonNode item) {
        long id = item.path("id").asLong();
        String title = item.path("name").asText("Shelter");
        String detail = item.path("description").asText() + "\n📍 " + item.path("address").asText()
                + " | Capacity: " + item.path("capacity").asText();
        return buildCatalogCard(item, id, title, detail, "/shelters/" + id + "/react", () -> beginEdit(item));
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong(); existingImageUrl = imageUrlFrom(item);
        nameField.setText(item.path("name").asText(""));
        addressField.setText(item.path("address").asText(""));
        phoneField.setText(item.path("phone").asText(""));
        emailField.setText(item.path("email").asText(""));
        descField.setText(item.path("description").asText(""));
        capacityField.setText(item.path("capacity").asText(""));
        cityField.setText(item.path("city").asText(""));
        imageUrlField.setText(existingImageUrl); addForm.setVisible(true); addForm.setManaged(true);
    }

    private void showForm(boolean show) {
        addForm.setVisible(show); addForm.setManaged(show);
        if (!show) {
            nameField.clear(); addressField.clear(); phoneField.clear(); emailField.clear();
            descField.clear(); capacityField.clear(); cityField.clear();
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null; existingImageUrl = null;
        }
    }
}
