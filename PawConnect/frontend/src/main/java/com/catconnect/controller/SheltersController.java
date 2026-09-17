package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SheltersController extends BaseListController {

    @FXML private Label screenTitle;
    @FXML private VBox addForm;
    @FXML private TextField nameField, addressField, phoneField, emailField, capacityField, cityField, websiteField, imageUrlField;
    @FXML private TextArea descField;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;
    @FXML private ComboBox<String> locationBox;
    @FXML private TextField searchField;
    @FXML private VBox adminActionBar;
    @FXML private Button addShelterButton;
    
    private Long editingId;
    private String existingImageUrl;

    @FXML 
    public void initialize() {
        boolean admin = isAdmin();

        if (addShelterButton != null) {
            addShelterButton.setVisible(admin);
            addShelterButton.setManaged(admin);
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
            adminActionBar.setVisible(admin);
            adminActionBar.setManaged(admin);
        }

        loadData();
    }

    private boolean isAdmin() {
        return com.catconnect.util.Session.getCurrentUser() != null
                && com.catconnect.util.Session.getCurrentUser().isAdmin();
    }

    @FXML
    private void onSearch() {
        resetListState();
        loadData();
    }

    @FXML 
    private void onAdd() { 
        if (!isAdmin()) {
            com.catconnect.util.UiHelper.showError("Only admin can add shelter listings.");
            return;
        }
        showForm(true); 
    }
    
    @FXML private void onRefresh() { loadData(); }
    @FXML private void onCancelAdd() { showForm(false); }
    @FXML private void onChooseImage() { chooseImage(photoPreview, photoLabel); }
    @FXML private void onClearImage() { clearSelectedImage(photoPreview, photoLabel, imageUrlField); }

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
        body.put("email", emailField.getText().trim());
        body.put("description", descField.getText().trim());
        body.put("city", city);
        body.put("mapLink", websiteField.getText().trim());
        try { 
            body.put("capacity", Integer.parseInt(capacityField.getText().trim())); 
        } catch (NumberFormatException ignored) {}

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
            updateWithImage("/admin/shelters/" + id, body, img, url);
        } else {
            postWithImage("/admin/shelters", body, img, url);
        }
    }

    @Override 
    protected String getApiPath() { 
        String base = "/shelters";
        String loc = locationBox != null ? locationBox.getValue() : null;
        if (loc != null && !loc.trim().isEmpty() && !"All".equalsIgnoreCase(loc)) {
            // filtering handled below
        }
        return base; 
    }
    
    @Override protected String getDeletePath() { return "/admin/shelters/"; }
    @Override protected String getUploadCategory() { return "shelters"; }
    
    @Override 
    protected VBox buildCard(JsonNode item) {
        String selected = locationBox == null || locationBox.getValue() == null
                ? "All"
                : locationBox.getValue().trim();
                
        String searchTxt = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        String city = item.path("city").asText("").trim();
        String address = item.path("address").asText("").trim();
        String name = item.path("name").asText("").trim();
        String desc = item.path("description").asText("").trim();

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
        
        if (!searchTxt.isEmpty()) {
            if (!name.toLowerCase().contains(searchTxt) && !desc.toLowerCase().contains(searchTxt)
                 && !city.toLowerCase().contains(searchTxt)) {
                VBox hiddenBox = new VBox();
                hiddenBox.setVisible(false);
                hiddenBox.setManaged(false); 
                return hiddenBox;
            }
        }

        double userLat = com.catconnect.util.Session.getUserLat();
        double userLon = com.catconnect.util.Session.getUserLon();
        double itemLat = item.path("latitude").asDouble(0);
        double itemLon = item.path("longitude").asDouble(0);

        double distanceKm = 0;
        if (userLat != 0 && userLon != 0 && itemLat != 0 && itemLon != 0) {
            distanceKm = calculateDistance(userLat, userLon, itemLat, itemLon);
        } else {
            if ("Dhanmondi".equalsIgnoreCase(city) || "Uttara".equalsIgnoreCase(city)) {
                distanceKm = 0.5 + Math.random() * 3;
            }
        }

        long id = item.path("id").asLong();
        String title = item.path("name").asText("Shelter");
        
        String distStr = distanceKm > 0 ? "\n\uD83D\uDCCF " + String.format("%.1f", distanceKm) + " km away" : "";
        
        String detail = "\u2606 0.0\n"
                + "\uD83D\uDCCD " + item.path("address").asText()
                + distStr
                + "\n\uD83D\uDCDE " + item.path("phone").asText()
                + "\n\uD83D\uDD52 " + item.path("capacity").asText();
                
        VBox card = buildCatalogCard(item, id, title, detail, "/shelters/" + id + "/react", () -> beginEdit(item));

        Button mapButton = new Button("\uD83D\uDCCD Open Map");
        mapButton.getStyleClass().add("secondary-button");
        mapButton.setOnAction(e -> openMap(item));

        Button callButton = new Button("\uD83D\uDCDE Call");
        callButton.getStyleClass().add("primary-button");
        callButton.setOnAction(e -> {
            Alert alert = new Alert(
                    Alert.AlertType.INFORMATION,
                    item.path("phone").asText("No phone available")
            );
            alert.setTitle("Shelter Contact");
            alert.setHeaderText(item.path("name").asText("Shelter"));
            alert.showAndWait();
        });

        Button websiteButton = new Button("\uD83C\uDF10 View Website");
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
        emailField.setText(item.path("email").asText(""));
        descField.setText(item.path("description").asText(""));
        capacityField.setText(item.path("capacity").asText(""));
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
            emailField.clear();
            descField.clear(); 
            capacityField.clear(); 
            cityField.clear();
            websiteField.clear();
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null; 
            existingImageUrl = null;
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
