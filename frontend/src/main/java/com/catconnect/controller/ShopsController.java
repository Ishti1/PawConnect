package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.HBox;

public class ShopsController extends BaseListController {

    @FXML private Label screenTitle;
    @FXML private VBox addForm;
    @FXML private TextField nameField, addressField, phoneField, hoursField, cityField, websiteField, imageUrlField;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;
    @FXML private ComboBox<String> locationBox;
    @FXML private Button addShopButton;
    @FXML private VBox adminActionBar;
    @FXML private TextField searchField;
    private Long editingId;
    private String existingImageUrl;

    @FXML
    public void initialize() {
        boolean admin = com.catconnect.util.Session.getCurrentUser() != null
                && com.catconnect.util.Session.getCurrentUser().isAdmin();

        if (addShopButton != null) {
            addShopButton.setVisible(admin);
            addShopButton.setManaged(admin);
        }

        if (addForm != null) {
            addForm.setVisible(false);
            addForm.setManaged(false);
        }

        if (locationBox != null) {
            locationBox.getItems().setAll("All", "Dhanmondi", "Uttara");
            locationBox.setValue("All");
        }

        if (adminActionBar != null) {
            adminActionBar.setVisible(isAdmin());
            adminActionBar.setManaged(isAdmin());
        }

        loadData();
    }

    @FXML
    private void onSearch() {
        resetListState();
        loadData();
    }

    @FXML
    private void onAdd() {
        if (!isAdmin()) {
            com.catconnect.util.UiHelper.showError("Only admin can add cat shop listings.");
            return;
        }
        showForm(true);
    }

    @FXML private void onRefresh() { loadData(); }
    @FXML private void onCancelAdd() { showForm(false); }
    @FXML private void onChooseImage() { chooseImage(photoPreview, photoLabel); }
    @FXML private void onClearImage() { clearSelectedImage(photoPreview, photoLabel, imageUrlField); }

    @FXML
    private void onFindNearby() {
        resetListState();
        loadData();
    }

    private boolean isAdmin() {
        return com.catconnect.util.Session.getCurrentUser() != null
                && com.catconnect.util.Session.getCurrentUser().isAdmin();
    }

    @Override
    protected String getApiPath() {
        return "/cat-shops";
    }

    @FXML
    private void onSave() {
        if (nameField.getText().isBlank()) {
            com.catconnect.util.UiHelper.showError("Name is required.");
            return;
        }

        String city = cityField.getText().trim();

        Map<String, Object> body = new HashMap<>();
        body.put("name", nameField.getText().trim());
        body.put("address", addressField.getText().trim());
        body.put("phone", phoneField.getText().trim());
        body.put("openHours", hoursField.getText().trim());
        body.put("city", city);
        body.put("mapLink", websiteField.getText().trim());

        if ("Dhanmondi".equalsIgnoreCase(city)) {
            body.put("latitude", 23.7465);
            body.put("longitude", 90.3760);
        } else if ("Uttara".equalsIgnoreCase(city)) {
            body.put("latitude", 23.8759);
            body.put("longitude", 90.3795);
        }

        File img = selectedImageFile;
        String url = readImageUrlField(imageUrlField);

        if (editingId != null && img == null && url.isBlank() && existingImageUrl != null) {
            url = existingImageUrl;
        }

        Long id = editingId;
        showForm(false);

        if (id != null) {
            updateWithImage("/admin/cat-shops/" + id, body, img, url);
        } else {
            postWithImage("/admin/cat-shops", body, img, url);
        }
    }

    @Override protected String getDeletePath() { return "/admin/cat-shops/"; }

    @Override
    protected String getUploadCategory() {
        return "cat-shops";
    }

    @Override
    protected VBox buildCard(JsonNode item) {
        String selected = locationBox == null || locationBox.getValue() == null
                ? "All"
                : locationBox.getValue().trim();

        String city = item.path("city").asText("").trim();
        String address = item.path("address").asText("").trim();

        double distanceKm = 0;

        if ("Dhanmondi".equalsIgnoreCase(city)) {
            distanceKm = 0.5 + Math.random() * 3;
        } else if ("Uttara".equalsIgnoreCase(city)) {
            distanceKm = 0.5 + Math.random() * 3;
        }

        if (!"All".equalsIgnoreCase(selected)) {
            String selectedLower = selected.toLowerCase();
            String cityLower = city.toLowerCase();
            String addressLower = address.toLowerCase();

            if (!cityLower.contains(selectedLower) && !addressLower.contains(selectedLower)) {
                VBox hiddenBox = new VBox();
                hiddenBox.setVisible(false);
                hiddenBox.setManaged(false);
                return hiddenBox;
            }
        }

        long id = item.path("id").asLong();
        String title = item.path("name").asText("Cat Shop");

        String detail =
                "⭐ " + item.path("rating").asText("4.5")
                        + "\n📍 " + address
                        + "\n📏 " + String.format("%.1f", distanceKm) + " km away"
                        + "\n📞 " + item.path("phone").asText()
                        + "\n🕒 " + item.path("openHours").asText();

        VBox card = buildCatalogCard(
                item,
                id,
                title,
                detail,
                "/cat-shops/" + id + "/react",
                () -> beginEdit(item)
        );

        card.setMaxWidth(Double.MAX_VALUE);
        card.setFillWidth(true);

        Button mapButton = new Button("📍 Open Map");
        mapButton.getStyleClass().add("secondary-button");
        mapButton.setOnAction(e -> openMap(item));

        Button callButton = new Button("📞 Call");
        callButton.getStyleClass().add("primary-button");
        callButton.setOnAction(e -> {
            Alert alert = new Alert(
                    Alert.AlertType.INFORMATION,
                    item.path("phone").asText("No phone available")
            );
            alert.setTitle("Shop Contact");
            alert.setHeaderText(item.path("name").asText("Cat Shop"));
            alert.showAndWait();
        });

        Button websiteButton = new Button("🌐 View Website");
        websiteButton.getStyleClass().add("secondary-button");
        websiteButton.setOnAction(e -> openWebsite(item.path("mapLink").asText("")));

        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.getChildren().addAll(mapButton, callButton, websiteButton);

        card.getChildren().add(actions);

        return card;
    }

    private void openMap(JsonNode item) {
        try {
            double lat = item.path("latitude").asDouble(0);
            double lng = item.path("longitude").asDouble(0);

            String query;

            if (lat != 0 && lng != 0) {
                query = lat + "," + lng;
            } else {
                String address = item.path("address").asText("").trim();
                String city = item.path("city").asText("").trim();
                query = (address + " " + city).trim().replace(" ", "+");
            }

            String url = "https://www.google.com/maps/search/?api=1&query=" + query;
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));

        } catch (Exception e) {
            com.catconnect.util.UiHelper.showError("Could not open map.");
        }
    }

    private void openWebsite(String url) {
        try {
            if (url == null || url.isBlank()) {
                com.catconnect.util.UiHelper.showError("No website available.");
                return;
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));

        } catch (Exception e) {
            com.catconnect.util.UiHelper.showError("Could not open website.");
        }
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong();
        existingImageUrl = imageUrlFrom(item);

        nameField.setText(item.path("name").asText(""));
        addressField.setText(item.path("address").asText(""));
        phoneField.setText(item.path("phone").asText(""));
        hoursField.setText(item.path("openHours").asText(""));
        cityField.setText(item.path("city").asText(""));
        websiteField.setText(item.path("mapLink").asText(""));
        imageUrlField.setText(existingImageUrl);

        addForm.setVisible(true);
        addForm.setManaged(true);
    }

    private void showForm(boolean show) {
        addForm.setVisible(show);
        addForm.setManaged(show);

        if (!show) {
            nameField.clear();
            addressField.clear();
            phoneField.clear();
            hoursField.clear();
            cityField.clear();
            websiteField.clear();
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null;
            existingImageUrl = null;
        }
    }
}