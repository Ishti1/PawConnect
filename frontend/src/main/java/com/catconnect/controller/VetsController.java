package com.catconnect.controller;

import com.catconnect.service.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.HBox;


public class VetsController extends BaseListController {

    @FXML private Label screenTitle;
    @FXML private VBox addForm;
    @FXML private TextField nameField, addressField, phoneField, hoursField, cityField, websiteField, imageUrlField;
    @FXML private CheckBox emergencyBox;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;
    @FXML private ComboBox<String> locationBox;
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
    private boolean isAdmin() {
        return com.catconnect.util.Session.getCurrentUser() != null
                && com.catconnect.util.Session.getCurrentUser().isAdmin();
    }
    @Override
    protected String getApiPath() {
        return emergencyOnly ? "/vets/emergency" : "/vets";
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

        card.setFillWidth(true);

        Button mapButton = new Button("🐾 View on Paw Map");
        mapButton.getStyleClass().add("secondary-button");
        mapButton.setOnAction(e -> openPawMap(item));

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

    private void openPawMap(JsonNode item) {
        double latitude = item.path("latitude").asDouble(0);
        double longitude = item.path("longitude").asDouble(0);

        String name = item.path("name").asText("Veterinary Clinic");
        String type = item.path("emergency").asBoolean(false)
                ? "Emergency Vet"
                : "Veterinary Clinic";

        /*
         * Most PawConnect vet records already contain coordinates.
         * In that case we can open Paw Map immediately.
         */
        if (isValidMapCoordinate(latitude, longitude)) {
            navigateToPawMap(latitude, longitude, name, type);
            return;
        }

        /*
         * Older or newly-added records may not have coordinates yet.
         * Fall back to the same backend geocoder used by PawConnect
         * rather than opening Google Maps.
         */
        String address = item.path("address").asText("").trim();
        String city = item.path("city").asText("").trim();
        String query = (address + ", " + city).replaceAll("^,\\s*|,\\s*$", "").trim();

        if (query.isBlank()) {
            com.catconnect.util.UiHelper.showError(
                    "This vet does not have a usable address or map coordinates."
            );
            return;
        }

        Thread geocodeThread = new Thread(() -> {
            try {
                String path = "/map/geocode?query="
                        + URLEncoder.encode(query, StandardCharsets.UTF_8);

                JsonNode response = ApiClient.get().getList(path);

                if (response == null || !response.path("success").asBoolean(false)) {
                    String message = response == null
                            ? "Could not find this vet on Paw Map."
                            : response.path("message")
                            .asText("Could not find this vet on Paw Map.");

                    Platform.runLater(() ->
                            com.catconnect.util.UiHelper.showError(message)
                    );
                    return;
                }

                double geocodedLat = response.path("latitude").asDouble(0);
                double geocodedLon = response.path("longitude").asDouble(0);

                if (!isValidMapCoordinate(geocodedLat, geocodedLon)) {
                    Platform.runLater(() ->
                            com.catconnect.util.UiHelper.showError(
                                    "The vet location returned invalid coordinates."
                            )
                    );
                    return;
                }

                Platform.runLater(() ->
                        navigateToPawMap(
                                geocodedLat,
                                geocodedLon,
                                name,
                                type
                        )
                );

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() ->
                        com.catconnect.util.UiHelper.showError(
                                "Could not find this vet on Paw Map."
                        )
                );
            }
        }, "paw-map-vet-geocode");

        geocodeThread.setDaemon(true);
        geocodeThread.start();
    }

    private void navigateToPawMap(
            double latitude,
            double longitude,
            String name,
            String type
    ) {
        MapController.setPendingTarget(
                latitude,
                longitude,
                name,
                type
        );

        MainController main = MainController.getInstance();

        if (main == null) {
            com.catconnect.util.UiHelper.showError(
                    "Could not open Paw Map."
            );
            return;
        }

        main.navigateTo("map");

        /*
         * MainController performs navigation on the JavaFX thread.
         * Queue the focus request after that navigation so cached maps
         * are also recentered correctly.
         */
        Platform.runLater(
                MapController::focusPendingTargetIfPossible
        );
    }

    private boolean isValidMapCoordinate(
            double latitude,
            double longitude
    ) {
        return Double.isFinite(latitude)
                && Double.isFinite(longitude)
                && latitude >= -90
                && latitude <= 90
                && longitude >= -180
                && longitude <= 180
                && !(latitude == 0 && longitude == 0);
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
            emergencyBox.setSelected(emergencyOnly);
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null;
            existingImageUrl = null;
        } else if (emergencyOnly) {
            emergencyBox.setSelected(true);
        }
    }
}