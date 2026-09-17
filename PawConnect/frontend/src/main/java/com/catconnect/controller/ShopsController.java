package com.catconnect.controller;

import com.catconnect.service.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ShopsController extends BaseListController {

    @FXML private VBox addForm;
    @FXML private TextField nameField, addressField, phoneField, hoursField, cityField, websiteField, ratingField, imageUrlField;
    @FXML private TextField searchField;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;
    private Long editingId;
    private String existingImageUrl;

    @FXML public void initialize() { loadData(); }
    @FXML private void onAdd() { showForm(true); }
    @FXML private void onRefresh() { loadData(); }
    @FXML private void onSearch() {
        resetListState();
        loadData();
    }
    @FXML private void onCancelAdd() { showForm(false); }
    @FXML private void onChooseImage() { chooseImage(photoPreview, photoLabel); }
    @FXML private void onClearImage() { clearSelectedImage(photoPreview, photoLabel, imageUrlField); }

    @FXML private void onSave() {
        if (nameField.getText().isBlank()) { com.catconnect.util.UiHelper.showError("Name is required."); return; }
        Map<String, Object> body = new HashMap<>();
        body.put("name", nameField.getText().trim());
        body.put("address", addressField.getText().trim());
        body.put("phone", phoneField.getText().trim());
        if (hoursField != null) body.put("openHours", hoursField.getText().trim());
        body.put("city", cityField.getText().trim());
        if (websiteField != null) body.put("mapLink", websiteField.getText().trim());
        if (ratingField != null && !ratingField.getText().isBlank()) {
            try {
                body.put("rating", Double.parseDouble(ratingField.getText().trim()));
            } catch (NumberFormatException e) {
                // ignore invalid rating format
            }
        }
        File img = selectedImageFile; String url = readImageUrlField(imageUrlField);
        if (editingId != null && img == null && url.isBlank() && existingImageUrl != null) url = existingImageUrl;
        Long id = editingId; showForm(false);
        if (id != null) updateWithImage("/admin/shops/" + id, body, img, url);
        else postWithImage("/admin/shops", body, img, url);
    }

    @Override protected String getApiPath() { return "/shops"; }
    @Override protected String getDeletePath() { return "/admin/shops/"; }
    @Override protected String getUploadCategory() { return "shops"; }
    @Override
    protected VBox buildCard(JsonNode item) {
        String search = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        String shopName = item.path("name").asText("Shop");
        String shopAddress = item.path("address").asText("");
        String shopCity = item.path("city").asText("");

        if (!search.isBlank()) {
            String searchable = (shopName + " " + shopAddress + " " + shopCity).toLowerCase();
            if (!searchable.contains(search)) {
                VBox hidden = new VBox();
                hidden.setVisible(false);
                hidden.setManaged(false);
                return hidden;
            }
        }

        long id = item.path("id").asLong();
        String title = shopName;
        String detail = shopAddress
                + " | 📞 " + item.path("phone").asText()
                + " | ★ " + item.path("rating").asText()
                + " | " + item.path("city").asText();

        VBox card = buildCatalogCard(
                item,
                id,
                title,
                detail,
                "/shops/" + id + "/react",
                () -> beginEdit(item)
        );

        Button mapButton = new Button("🐾 View on Paw Map");
        mapButton.getStyleClass().add("secondary-button");
        mapButton.setOnAction(e -> openPawMap(item));

        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.getChildren().add(mapButton);

        card.getChildren().add(actions);

        return card;
    }

    private void openPawMap(JsonNode item) {
        double latitude = item.path("latitude").asDouble(0);
        double longitude = item.path("longitude").asDouble(0);

        String name = item.path("name").asText("Cat Shop");
        String type = "Cat Shop";

        if (isValidMapCoordinate(latitude, longitude)) {
            navigateToPawMap(latitude, longitude, name, type);
            return;
        }

        String address = item.path("address").asText("").trim();
        String city = item.path("city").asText("").trim();
        String query = (address + ", " + city).replaceAll("^,\\s*|,\\s*$", "").trim();

        if (query.isBlank()) {
            com.catconnect.util.UiHelper.showError(
                    "This shop does not have a usable address or map coordinates."
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
                            ? "Could not find this shop on Paw Map."
                            : response.path("message")
                            .asText("Could not find this shop on Paw Map.");

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
                                    "The shop location returned invalid coordinates."
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
                                "Could not find this shop on Paw Map."
                        )
                );
            }
        }, "paw-map-shop-geocode");

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

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong(); existingImageUrl = imageUrlFrom(item);
        nameField.setText(item.path("name").asText(""));
        addressField.setText(item.path("address").asText(""));
        phoneField.setText(item.path("phone").asText(""));
        cityField.setText(item.path("city").asText(""));
        if (hoursField != null) hoursField.setText(item.path("openHours").asText(""));
        if (websiteField != null) websiteField.setText(item.path("mapLink").asText(""));
        if (ratingField != null && item.has("rating")) ratingField.setText(item.path("rating").asText(""));
        imageUrlField.setText(existingImageUrl); addForm.setVisible(true); addForm.setManaged(true);
    }

    private void showForm(boolean show) {
        addForm.setVisible(show); addForm.setManaged(show);
        if (!show) { nameField.clear(); addressField.clear(); phoneField.clear(); cityField.clear();
            if (hoursField != null) hoursField.clear();
            if (websiteField != null) websiteField.clear();
            if (ratingField != null) ratingField.clear();
            clearSelectedImage(photoPreview, photoLabel, imageUrlField); editingId = null; existingImageUrl = null; }
    }
}
