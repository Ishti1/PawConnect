package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LostFoundController extends BaseListController {

    @FXML private VBox addForm;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField descField;
    @FXML private TextField locationField;
    @FXML private TextField addressLinkField;
    @FXML private TextField phoneField;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;
    @FXML private TextField imageUrlField;

    private Long editingId;
    private String existingImageUrl;

    @FXML
    public void initialize() {
        typeBox.getItems().addAll("LOST", "FOUND");
        typeBox.setValue("LOST");
        loadData();
    }

    @FXML private void onAdd()       { showForm(true); }
    @FXML private void onRefresh()   { loadData(); }
    @FXML private void onCancelAdd() { showForm(false); }
    @FXML private void onChooseImage() { chooseImage(photoPreview, photoLabel); }
    @FXML private void onClearImage()  { clearSelectedImage(photoPreview, photoLabel, imageUrlField); }

    @FXML
    private void onSave() {
        if (descField.getText().isBlank()) {
            com.catconnect.util.UiHelper.showError("Description is required.");
            return;
        }
        boolean isEditing = editingId != null;
        Map<String, Object> body = new HashMap<>();
        body.put("postType", typeBox.getValue());
        body.put("catDescription", descField.getText().trim());
        body.put("lastSeenLocation", locationField.getText().trim());
        body.put("mapLink", addressLinkField.getText().trim());
        body.put("contactPhone", phoneField.getText().trim());
        File imageFile = selectedImageFile;
        String imageUrl = readImageUrlField(imageUrlField);
        if (isEditing && imageFile == null && imageUrl.isBlank() && existingImageUrl != null && !existingImageUrl.isBlank()) {
            imageUrl = existingImageUrl;
        }
        Long id = editingId;
        showForm(false);
        if (isEditing) {
            updateWithImage("/lost-found/" + id, body, imageFile, imageUrl);
        } else {
            postWithImage("/lost-found", body, imageFile, imageUrl);
        }
    }

    @Override protected String getApiPath()         { return "/lost-found"; }
    @Override protected String getDeletePath()      { return "/lost-found/"; }
    @Override protected String getUploadCategory()  { return "lost-found"; }

    @Override
    protected VBox buildCard(JsonNode item) {
        long id = item.path("id").asLong();
        String type = item.path("postType").asText("?");
        String desc = item.path("catDescription").asText("(no description)");
        String loc = item.path("lastSeenLocation").asText("Unknown location");
        String phone = item.path("contactPhone").asText("No phone");
        String title = "[" + type + "] " + desc;
        String detail = "📍 " + loc + "   📞 " + phone;

        VBox card = buildCatalogCard(item, id, title, detail, "/lost-found/" + id + "/react", () -> beginEdit(item));
        Button addressBtn = addressLinkButton(item.path("mapLink").asText(""));
        if (addressBtn != null) {
            card.getChildren().add(new HBox(addressBtn));
        }
        return card;
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong();
        existingImageUrl = imageUrlFrom(item);
        typeBox.setValue(item.path("postType").asText("LOST"));
        descField.setText(item.path("catDescription").asText(""));
        locationField.setText(item.path("lastSeenLocation").asText(""));
        addressLinkField.setText(item.path("mapLink").asText(""));
        phoneField.setText(item.path("contactPhone").asText(""));
        imageUrlField.setText(existingImageUrl);
        addForm.setVisible(true);
        addForm.setManaged(true);
    }

    private void showForm(boolean show) {
        addForm.setVisible(show);
        addForm.setManaged(show);
        if (!show) {
            descField.clear();
            locationField.clear();
            addressLinkField.clear();
            phoneField.clear();
            typeBox.setValue("LOST");
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null;
            existingImageUrl = null;
        }
    }
}
