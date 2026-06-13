package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FoodController extends BaseListController {

    @FXML private VBox addForm;
    @FXML private TextField brandField, productField, ageField, healthField, imageUrlField;
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
        if (brandField.getText().isBlank() || productField.getText().isBlank()) {
            com.catconnect.util.UiHelper.showError("Brand and product name are required."); return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("brand", brandField.getText().trim());
        body.put("productName", productField.getText().trim());
        body.put("ageGroup", ageField.getText().trim());
        body.put("healthCondition", healthField.getText().trim());
        body.put("description", descField.getText().trim());
        File img = selectedImageFile; String url = readImageUrlField(imageUrlField);
        if (editingId != null && img == null && url.isBlank() && existingImageUrl != null) url = existingImageUrl;
        Long id = editingId; showForm(false);
        if (id != null) updateWithImage("/food-recommendations/" + id, body, img, url);
        else postWithImage("/food-recommendations", body, img, url);
    }

    @Override protected String getApiPath() { return "/food-recommendations"; }
    @Override protected String getDeletePath() { return "/food-recommendations/"; }
    @Override protected String getUploadCategory() { return "food"; }
    @Override protected VBox buildCard(JsonNode item) {
        long id = item.path("id").asLong();
        String title = item.path("brand").asText() + " - " + item.path("productName").asText();
        String detail = "Age: " + item.path("ageGroup").asText() + " | " + item.path("healthCondition").asText()
                + "\n" + item.path("description").asText() + " | ★ " + item.path("rating").asText();
        return buildCatalogCard(item, id, title, detail, "/food-recommendations/" + id + "/react", () -> beginEdit(item));
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong(); existingImageUrl = imageUrlFrom(item);
        brandField.setText(item.path("brand").asText(""));
        productField.setText(item.path("productName").asText(""));
        ageField.setText(item.path("ageGroup").asText(""));
        healthField.setText(item.path("healthCondition").asText(""));
        descField.setText(item.path("description").asText(""));
        imageUrlField.setText(existingImageUrl); addForm.setVisible(true); addForm.setManaged(true);
    }

    private void showForm(boolean show) {
        addForm.setVisible(show); addForm.setManaged(show);
        if (!show) {
            brandField.clear(); productField.clear(); ageField.clear(); healthField.clear(); descField.clear();
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null; existingImageUrl = null;
        }
    }
}
