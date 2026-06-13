package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ShopsController extends BaseListController {

    @FXML private VBox addForm;
    @FXML private TextField nameField, addressField, phoneField, cityField, imageUrlField;
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
        body.put("city", cityField.getText().trim());
        File img = selectedImageFile; String url = readImageUrlField(imageUrlField);
        if (editingId != null && img == null && url.isBlank() && existingImageUrl != null) url = existingImageUrl;
        Long id = editingId; showForm(false);
        if (id != null) updateWithImage("/shops/" + id, body, img, url);
        else postWithImage("/shops", body, img, url);
    }

    @Override protected String getApiPath() { return "/shops"; }
    @Override protected String getDeletePath() { return "/shops/"; }
    @Override protected String getUploadCategory() { return "shops"; }
    @Override protected VBox buildCard(JsonNode item) {
        long id = item.path("id").asLong();
        String title = item.path("name").asText("Shop");
        String detail = item.path("address").asText() + " | 📞 " + item.path("phone").asText()
                + " | ★ " + item.path("rating").asText() + " | " + item.path("city").asText();
        return buildCatalogCard(item, id, title, detail, "/shops/" + id + "/react", () -> beginEdit(item));
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong(); existingImageUrl = imageUrlFrom(item);
        nameField.setText(item.path("name").asText(""));
        addressField.setText(item.path("address").asText(""));
        phoneField.setText(item.path("phone").asText(""));
        cityField.setText(item.path("city").asText(""));
        imageUrlField.setText(existingImageUrl); addForm.setVisible(true); addForm.setManaged(true);
    }

    private void showForm(boolean show) {
        addForm.setVisible(show); addForm.setManaged(show);
        if (!show) { nameField.clear(); addressField.clear(); phoneField.clear(); cityField.clear();
            clearSelectedImage(photoPreview, photoLabel, imageUrlField); editingId = null; existingImageUrl = null; }
    }
}
