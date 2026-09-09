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
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import javafx.scene.layout.StackPane;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;


public class VetsController extends BaseListController {

    @FXML private Label screenTitle;
    @FXML private VBox addForm;
    @FXML private TextField nameField, addressField, phoneField, hoursField, cityField, websiteField, imageUrlField, ratingField;
    @FXML private CheckBox emergencyBox;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;
    @FXML private Label userLocationLabel;
    @FXML private Button addVetButton;
    @FXML private VBox adminActionBar;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryBox;
    
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

    @FXML
    public void initialize() {
        boolean admin = com.catconnect.util.Session.getCurrentUser() != null
                && com.catconnect.util.Session.getCurrentUser().isAdmin();

        if (addVetButton != null) {
            addVetButton.setVisible(admin);
            addVetButton.setManaged(admin);
        }

        if (addForm != null) {
            addForm.setVisible(false);
            addForm.setManaged(false);
        }


        if (userLocationLabel != null) {
            String selected = com.catconnect.util.Session.getSelectedLocation();
            if (selected != null && !selected.trim().isEmpty() && !"All".equalsIgnoreCase(selected)) {
                userLocationLabel.setText("📍 Location: " + selected);
            } else {
                userLocationLabel.setText("📍 Showing all locations");
            }
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
        boolean admin = com.catconnect.util.Session.getCurrentUser() != null
                && com.catconnect.util.Session.getCurrentUser().isAdmin();

        if (!isAdmin()) {
            com.catconnect.util.UiHelper.showError("Only admin can add vet listings.");
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

    @Override
    protected void onRefreshData() {
        if (userLocationLabel != null) {
            String selected = com.catconnect.util.Session.getSelectedLocation();
            if (selected != null && !selected.trim().isEmpty() && !"All".equalsIgnoreCase(selected)) {
                userLocationLabel.setText("📍 Location: " + selected);
            } else {
                userLocationLabel.setText("📍 Showing all locations");
            }
        }
    }
    private boolean isAdmin() {
        return com.catconnect.util.Session.getCurrentUser() != null
                && com.catconnect.util.Session.getCurrentUser().isAdmin();
    }
    @Override
    protected String getApiPath() {
        String base = emergencyOnly ? "/vets/emergency" : "/vets";
        String loc = com.catconnect.util.Session.getSelectedLocation();
        if (loc != null && !loc.trim().isEmpty() && !"All".equalsIgnoreCase(loc)) {
            try {
                return base + "?location=" + java.net.URLEncoder.encode(loc, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {}
        }
        return base;
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
        body.put("emergency", emergencyBox.isSelected());
        try {
            if (!ratingField.getText().isBlank()) {
                body.put("rating", Double.parseDouble(ratingField.getText().trim()));
            }
        } catch (Exception e) {}

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
            updateWithImage("/admin/vets/" + id, body, img, url);
        } else {
            postWithImage("/admin/vets", body, img, url);
        }
    }

    @Override protected String getDeletePath() { return "/admin/vets/"; }

    @Override
    protected String getUploadCategory() {
        return "vets";
    }

    @Override
    protected VBox buildCard(JsonNode item) {
        String selected = com.catconnect.util.Session.getSelectedLocation();
        if (selected == null || selected.trim().isEmpty()) {
            selected = "All";
        }
        selected = selected.trim();

        String city = item.path("city").asText("").trim();
        String address = item.path("address").asText("").trim();

        double userLat = com.catconnect.util.Session.getUserLat();
        double userLon = com.catconnect.util.Session.getUserLon();
        double vetLat = item.path("latitude").asDouble(0);
        double vetLon = item.path("longitude").asDouble(0);

        double distanceKm = 0;
        if (userLat != 0 && userLon != 0 && vetLat != 0 && vetLon != 0) {
            distanceKm = calculateDistance(userLat, userLon, vetLat, vetLon);
        } else {
            if ("Dhanmondi".equalsIgnoreCase(city)) {
                distanceKm = 0.5 + Math.random() * 3;
            } else if ("Uttara".equalsIgnoreCase(city)) {
                distanceKm = 0.5 + Math.random() * 3;
            }
        }

        if (!"All".equalsIgnoreCase(selected)) {
            String selectedLower = selected.toLowerCase();
            String cityLower = city.toLowerCase();
            String addressLower = address.toLowerCase();

            if (!cityLower.contains(selectedLower) && !addressLower.contains(selectedLower)) {
                VBox hiddenBox = new VBox();
                hiddenBox.setVisible(false);
                hiddenBox.setManaged(false); // Tells TilePane to collapse the space!
                return hiddenBox;
            }
        }

        long id = item.path("id").asLong();
        String em = item.path("emergency").asBoolean() ? " [EMERGENCY]" : "";
        String title = item.path("name").asText("Vet") + em;

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
                "/vets/" + id + "/react",
                () -> beginEdit(item)
        );

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
            alert.setTitle("Vet Contact");
            alert.setHeaderText(item.path("name").asText("Vet"));
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
        ratingField.setText(item.path("rating").asText(""));
        emergencyBox.setSelected(item.path("emergency").asBoolean(false));
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
            ratingField.clear();
            emergencyBox.setSelected(emergencyOnly);
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null;
            existingImageUrl = null;
        } else if (emergencyOnly) {
            emergencyBox.setSelected(true);
        }
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}