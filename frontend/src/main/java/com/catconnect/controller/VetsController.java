package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VetsController extends BaseListController {

    @FXML private Label screenTitle;
    @FXML private VBox addForm;
    @FXML private TextField nameField, addressField, phoneField, hoursField, cityField, imageUrlField;
    @FXML private CheckBox emergencyBox;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;

    private boolean emergencyOnly;
    private Boolean lastEmergencyMode;
    private Long editingId;
    private String existingImageUrl;

    public void setEmergencyOnly(boolean emergencyOnly) {
        boolean modeChanged = lastEmergencyMode == null || lastEmergencyMode != emergencyOnly;
        lastEmergencyMode = emergencyOnly;
        this.emergencyOnly = emergencyOnly;
        if (screenTitle != null) {
            screenTitle.setText(emergencyOnly ? "🚨 Emergency Vets (24/7)" : "🏥 Nearby Veterinarians");
        }
        if (modeChanged) {
            resetListState();
            loadData();
        }
    }

    @FXML public void initialize() { loadData(); }
    @FXML private void onAdd() { showForm(true); }
    @FXML private void onRefresh() { loadData(); }
    @FXML private void onCancelAdd() { showForm(false); }
    @FXML private void onChooseImage() { chooseImage(photoPreview, photoLabel); }
    @FXML private void onClearImage() { clearSelectedImage(photoPreview, photoLabel, imageUrlField); }

    @Override protected String getApiPath() { return emergencyOnly ? "/vets/emergency" : "/vets"; }

    @FXML private void onSave() {
        if (nameField.getText().isBlank()) { com.catconnect.util.UiHelper.showError("Name is required."); return; }
        Map<String, Object> body = new HashMap<>();
        body.put("name", nameField.getText().trim());
        body.put("address", addressField.getText().trim());
        body.put("phone", phoneField.getText().trim());
        body.put("openHours", hoursField.getText().trim());
        body.put("city", cityField.getText().trim());
        body.put("emergency", emergencyBox.isSelected());
        File img = selectedImageFile; String url = readImageUrlField(imageUrlField);
        if (editingId != null && img == null && url.isBlank() && existingImageUrl != null) url = existingImageUrl;
        Long id = editingId; showForm(false);
        if (id != null) updateWithImage("/vets/" + id, body, img, url);
        else postWithImage("/vets", body, img, url);
    }

    @Override protected String getDeletePath() { return "/vets/"; }
    @Override protected String getUploadCategory() { return "vets"; }

    @Override protected VBox buildCard(JsonNode item) {
        long id = item.path("id").asLong();
        String em = item.path("emergency").asBoolean() ? " [EMERGENCY]" : "";
        String title = item.path("name").asText("Vet") + em;
        String detail = "★ " + item.path("rating").asText() + " | " + item.path("address").asText()
                + "\n📞 " + item.path("phone").asText() + " | " + item.path("openHours").asText();
        return buildCatalogCard(item, id, title, detail, "/vets/" + id + "/react", () -> beginEdit(item));
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong(); existingImageUrl = imageUrlFrom(item);
        nameField.setText(item.path("name").asText(""));
        addressField.setText(item.path("address").asText(""));
        phoneField.setText(item.path("phone").asText(""));
        hoursField.setText(item.path("openHours").asText(""));
        cityField.setText(item.path("city").asText(""));
        emergencyBox.setSelected(item.path("emergency").asBoolean(false));
        imageUrlField.setText(existingImageUrl); addForm.setVisible(true); addForm.setManaged(true);
    }

    private void showForm(boolean show) {
        addForm.setVisible(show); addForm.setManaged(show);
        if (!show) {
            nameField.clear(); addressField.clear(); phoneField.clear(); hoursField.clear(); cityField.clear();
            emergencyBox.setSelected(emergencyOnly);
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null; existingImageUrl = null;
        } else if (emergencyOnly) {
            emergencyBox.setSelected(true);
        }
    }
}
