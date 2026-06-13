package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AdoptionController extends BaseListController {

    @FXML private VBox addForm;
    @FXML private TextField catNameField;
    @FXML private TextField breedField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderBox;
    @FXML private TextArea descField;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;
    @FXML private TextField imageUrlField;

    private Long editingId;
    private String existingImageUrl;

    @FXML
    public void initialize() {
        genderBox.getItems().addAll("Male", "Female", "Unknown");
        genderBox.setValue("Unknown");
        loadData();
    }

    @FXML private void onAdd()       { showForm(true); }
    @FXML private void onRefresh()   { loadData(); }
    @FXML private void onCancelAdd() { showForm(false); }
    @FXML private void onChooseImage() { chooseImage(photoPreview, photoLabel); }
    @FXML private void onClearImage()  { clearSelectedImage(photoPreview, photoLabel, imageUrlField); }

    @FXML
    private void onSave() {
        if (catNameField.getText().isBlank()) {
            com.catconnect.util.UiHelper.showError("Cat name is required.");
            return;
        }
        boolean isEditing = editingId != null;
        int ageMonths = 0;
        try {
            ageMonths = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException ignored) {
        }

        Map<String, Object> body = new HashMap<>();
        body.put("catName", catNameField.getText().trim());
        body.put("breed", breedField.getText().trim());
        body.put("ageMonths", ageMonths);
        body.put("gender", genderBox.getValue());
        body.put("description", descField.getText().trim());
        File imageFile = selectedImageFile;
        String imageUrl = readImageUrlField(imageUrlField);
        if (isEditing && imageFile == null && imageUrl.isBlank() && existingImageUrl != null && !existingImageUrl.isBlank()) {
            imageUrl = existingImageUrl;
        }
        Long id = editingId;
        showForm(false);
        if (isEditing) {
            updateWithImage("/adoptions/" + id, body, imageFile, imageUrl);
        } else {
            postWithImage("/adoptions", body, imageFile, imageUrl);
        }
    }

    @Override protected String getApiPath()        { return "/adoptions"; }
    @Override protected String getDeletePath()     { return "/adoptions/"; }
    @Override protected String getUploadCategory() { return "adoption"; }
    @Override
    protected VBox buildCard(JsonNode item) {
        long id = item.path("id").asLong();
        String name = item.path("catName").asText("");
        if (name.isEmpty()) {
            name = "Cat";
        }
        String breed = item.path("breed").asText();
        String gender = item.path("gender").asText();
        int age = item.path("ageMonths").asInt(0);
        String desc = item.path("description").asText();
        String title = name + (breed.isBlank() ? "" : " (" + breed + ")");
        String detail = gender + ", " + age + " months\n" + desc;

        return buildCatalogCard(item, id, title, detail, "/adoptions/" + id + "/react", () -> beginEdit(item));
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong();
        existingImageUrl = imageUrlFrom(item);
        catNameField.setText(item.path("catName").asText(""));
        breedField.setText(item.path("breed").asText(""));
        ageField.setText(String.valueOf(item.path("ageMonths").asInt(0)));
        genderBox.setValue(item.path("gender").asText("Unknown"));
        descField.setText(item.path("description").asText(""));
        imageUrlField.setText(existingImageUrl);
        addForm.setVisible(true);
        addForm.setManaged(true);
    }

    private void showForm(boolean show) {
        addForm.setVisible(show);
        addForm.setManaged(show);
        if (!show) {
            catNameField.clear();
            breedField.clear();
            ageField.clear();
            descField.clear();
            genderBox.setValue("Unknown");
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null;
            existingImageUrl = null;
        }
    }
}
