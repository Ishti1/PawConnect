package com.catconnect.controller;

import com.catconnect.service.ApiClient;
import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MainController {

    private static MainController instance;

    public MainController() {
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }


    @FXML private Label userLabel;
    @FXML private javafx.scene.control.ScrollPane contentScroll;
    @FXML private VBox contentBox;
    @FXML private VBox sidebar;
    @FXML private Label appTitle;
    @FXML private Button btnLogout;
    @FXML private Button btnHome;
    @FXML private Button btnLostFound;
    @FXML private Button btnVets;
    @FXML private Button btnEmergency;
    @FXML private Button btnFood;
    @FXML private Button btnShops;
    @FXML private Button btnMoments;
    @FXML private Button btnDonations;
    @FXML private Button btnShelters;
    @FXML private Button btnAdoption;
    @FXML private Button btnMemes;
    @FXML private Button btnChat;

    private final Map<String, Parent> screenCache = new HashMap<>();
    private final Map<String, Object> controllerCache = new HashMap<>();
    private String currentScreenKey;
    private Parent homeContent;
    private ChatController chatScreenController;


    @FXML
    public void initialize() {
        instance = this;
        if (Session.getCurrentUser() != null) {
            userLabel.setText("Hi, " + Session.getCurrentUser().getDisplayName());
        }
        
        // Sidebar hover logic
        if (sidebar != null) {
            sidebar.setOnMouseEntered(e -> expandSidebar());
            sidebar.setOnMouseExited(e -> minimizeSidebar());
            minimizeSidebar(); // start minimized
        }

        setActive(btnHome);
        renderHome();
    }
    
    private void expandSidebar() {
        sidebar.setPrefWidth(220);
        appTitle.setVisible(true);
        appTitle.setManaged(true);
        userLabel.setVisible(true);
        userLabel.setManaged(true);
        
        for (Button b : navButtons()) {
            if (b != null) setButtonText(b, true);
        }
        if (btnLogout != null) setButtonText(btnLogout, true);
    }

    private void minimizeSidebar() {
        sidebar.setPrefWidth(80);
        appTitle.setVisible(false);
        appTitle.setManaged(false);
        userLabel.setVisible(false);
        userLabel.setManaged(false);
        
        for (Button b : navButtons()) {
            if (b != null) setButtonText(b, false);
        }
        if (btnLogout != null) setButtonText(btnLogout, false);
    }

    private void setButtonText(Button btn, boolean expanded) {
        String fullText = (String) btn.getProperties().getOrDefault("fullText", btn.getText());
        btn.getProperties().putIfAbsent("fullText", fullText);
        
        if (expanded) {
            btn.setText(fullText);
            btn.setStyle(""); // reset inline style to use css class defaults
        } else {
            // Extract the first emoji/character
            int firstSpace = fullText.indexOf(' ');
            if (firstSpace > 0) {
                btn.setText(fullText.substring(0, firstSpace));
            } else if (fullText.length() >= 2) {
                // To handle surrogate pairs like emojis properly
                btn.setText(fullText.substring(0, Character.charCount(fullText.codePointAt(0))));
            } else {
                btn.setText(fullText.substring(0, 1));
            }
            // Remove side padding and center the text to prevent '...' truncation
            btn.setStyle("-fx-alignment: center; -fx-padding: 10 0; -fx-text-overrun: clip;");
        }
    }

    @FXML private void showHome() {
        setActive(btnHome);
        showCachedScreen("home", null, false, false);
    }

    @FXML private void showLostFound() {
        setActive(btnLostFound);
        showCachedScreen("lost-found", "/fxml/lost_found.fxml", false, false);
    }

    @FXML private void showMoments() {
        setActive(btnMoments);
        showCachedScreen("moments", "/fxml/moments.fxml", false, false);
    }

    @FXML private void showMemes() {
        setActive(btnMemes);
        showCachedScreen("memes", "/fxml/memes.fxml", false, false);
    }

    @FXML private void showAdoption() {
        setActive(btnAdoption);
        showCachedScreen("adoption", "/fxml/adoption.fxml", false, false);
    }

    @FXML private void showVets() {
        setActive(btnVets);
        showCachedScreen("vets", "/fxml/vets.fxml", false, false,
                c -> ((VetsController) c).setEmergencyOnly(false));
    }

    @FXML private void showEmergency() {
        setActive(btnEmergency);
        showCachedScreen("vets-emergency", "/fxml/vets.fxml", false, false,
                c -> ((VetsController) c).setEmergencyOnly(true));
    }

    @FXML private void showFood() {
        setActive(btnFood);
        showReadOnly("food", "Cat Food Recommendations", "/food-recommendations", this::renderFood);
    }

    @FXML private void showShops() {
        setActive(btnShops);
        showCachedScreen("shops", "/fxml/shops.fxml", false, false);
    }

    @FXML private void showDonations() {
        setActive(btnDonations);
        showCachedScreen("donations", "/fxml/donations.fxml", false, false);
    }

    @FXML private void showShelters() {
        setActive(btnShelters);
        showReadOnly("shelters", "Shelter Directory", "/shelters", this::renderShelters);
    }

    @FXML private void showChat() {
        setActive(btnChat);
        showCachedScreen("chat", "/fxml/chat.fxml", true, true);
    }

    public void navigateTo(String screen) {
        javafx.application.Platform.runLater(() -> {
            switch (screen) {
                case "vets": showVets(); break;
                case "adoption": showAdoption(); break;
                case "lost-found": showLostFound(); break;
                case "shops": showShops(); break;
                case "chat": showChat(); break;
                case "moments": showMoments(); break;
                case "donations": showDonations(); break;
                case "shelters": showShelters(); break;
                case "memes": showMemes(); break;
                default: showHome(); break;
            }
        });
    }

    @FXML
    private void onLogout() {
        disconnectChat();
        screenCache.clear();
        controllerCache.clear();
        currentScreenKey = null;
        homeContent = null;
        Session.clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) contentBox.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 1100, 700));
        } catch (Exception e) {
            UiHelper.showError(e.getMessage());
        }
    }

    /**
     * Reuse loaded screens instead of reloading FXML on every sidebar click.
     */
    private void showCachedScreen(String key, String fxmlPath, boolean isChat, boolean scrollTop) {
        showCachedScreen(key, fxmlPath, isChat, scrollTop, null);
    }

    private void showCachedScreen(String key, String fxmlPath, boolean isChat, boolean scrollTop,
                                  Consumer<Object> controllerSetup) {
        if ("home".equals(key)) {
            renderHome();
            currentScreenKey = key;
            return;
        }

        if (!isChat) {
            disconnectChat();
        }

        boolean returningToSame = key.equals(currentScreenKey);
        Parent root = screenCache.get(key);

        if (root == null && fxmlPath != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                root = loader.load();
                Object controller = loader.getController();
                screenCache.put(key, root);
                controllerCache.put(key, controller);
                if (isChat) {
                    chatScreenController = (ChatController) controller;
                }
                if (controllerSetup != null) {
                    controllerSetup.accept(controller);
                }
            } catch (Exception e) {
                UiHelper.showError("Failed to load screen: " + e.getMessage());
                return;
            }
        } else {
            if (isChat && controllerCache.get(key) instanceof ChatController chat) {
                chatScreenController = chat;
            }
            if (controllerCache.get(key) instanceof BaseListController listCtrl) {
                listCtrl.refreshData();
            }
            if (controllerSetup != null && controllerCache.containsKey(key)) {
                controllerSetup.accept(controllerCache.get(key));
            }
        }

        if (root == null) {
            return;
        }

        if (!contentBox.getChildren().contains(root)) {
            contentBox.getChildren().setAll(root);
        }

        if (contentScroll != null) {
            contentScroll.setPadding(new javafx.geometry.Insets(30, 40, 30, 40));
        }
        currentScreenKey = key;
        if (scrollTop && !returningToSame) {
            scrollToTop();
        }
    }
    // Paste this into MainController.java
    // Inside MainController.java
    public void openDirectChat(Long targetUserId, String userName) {
        setActive(btnChat);
        showCachedScreen("chat", "/fxml/chat.fxml", true, true, controller -> {
            if (controller instanceof ChatController chatController) {
                // Ensure you are passing BOTH arguments here
                chatController.openChatWithUser(targetUserId, userName);
            }
        });
    }
    private void showReadOnly(String key, String title, String path, Consumer<JsonNode> renderer) {
        // cache a simple VBox so switching tabs doesn't recreate layout
        Parent root = screenCache.get(key);
        VBox box;
        if (root instanceof VBox v) {
            box = v;
        } else {
            box = new VBox(16);
            box.getChildren().add(UiHelper.sectionTitle(title));
            Label loading = new Label("Loading...");
            loading.setId("loading");
            box.getChildren().add(loading);
            screenCache.put(key, box);
        }

        contentBox.getChildren().setAll(box);
        currentScreenKey = key;
        scrollToTop();

        // Always refresh data when opening read-only pages (but don't wipe whole app)
        new Thread(() -> {
            try {
                JsonNode data = ApiClient.get().getList(path);
                Platform.runLater(() -> {
                    box.getChildren().removeIf(n -> "loading".equals(n.getId()) || (n instanceof Label l && l.getText().startsWith("Error:")));
                    // Remove previously rendered cards but keep the title
                    while (box.getChildren().size() > 1) {
                        box.getChildren().remove(1);
                    }
                    renderer.accept(data);
                });
            } catch (Exception e) {
                Platform.runLater(() -> box.getChildren().add(new Label("Error: " + e.getMessage())));
            }
        }).start();
    }

    private void renderVets(JsonNode data) {
        VBox box = (VBox) contentBox.getChildren().getFirst();
        for (JsonNode v : data) {
            String em = v.path("emergency").asBoolean() ? " [EMERGENCY]" : "";
            box.getChildren().add(addressCard(
                    v.path("name").asText() + em,
                    v.path("mapLink").asText(""),
                    "★ " + v.path("rating").asText() + " | " + v.path("address").asText()
                            + "\n📞 " + v.path("phone").asText() + " | " + v.path("openHours").asText()
            ));
        }
    }

    private void renderShops(JsonNode data) {
        VBox box = (VBox) contentBox.getChildren().getFirst();
        for (JsonNode s : data) {
            box.getChildren().add(addressCard(
                    s.path("name").asText(),
                    s.path("mapLink").asText(""),
                    s.path("address").asText() + " | ★ " + s.path("rating").asText() + " | 📞 " + s.path("phone").asText()));
        }
    }

    private void renderShelters(JsonNode data) {
        VBox box = (VBox) contentBox.getChildren().getFirst();
        for (JsonNode s : data) {
            box.getChildren().add(addressCard(
                    s.path("name").asText(),
                    s.path("mapLink").asText(""),
                    s.path("description").asText() + "\n📍 " + s.path("address").asText() + " | Capacity: " + s.path("capacity").asText()));
        }
    }

    private void renderFood(JsonNode data) {
        VBox box = (VBox) contentBox.getChildren().getFirst();
        for (JsonNode f : data) {
            box.getChildren().add(UiHelper.card(
                    f.path("brand").asText() + " - " + f.path("productName").asText(),
                    "Age: " + f.path("ageGroup").asText() + " | " + f.path("healthCondition").asText()
                            + "\n" + f.path("description").asText() + " | ★ " + f.path("rating").asText()
            ));
        }
    }

    private VBox addressCard(String title, String mapLink, String detail) {
        VBox card = UiHelper.card(title, detail);
        if (mapLink != null && !mapLink.isBlank()) {
            Button mapBtn = new Button("Address Link");
            mapBtn.getStyleClass().add("secondary-button");
            String url = mapLink.trim();
            mapBtn.setOnAction(e -> openAddressLink(url));
            card.getChildren().add(new HBox(mapBtn));
        }
        return card;
    }

    private void openAddressLink(String mapLink) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(mapLink.trim()));
        } catch (Exception ex) {
            UiHelper.showError("Could not open address link. Paste a full URL (https://...).");
        }
    }

    private void disconnectChat() {
        // Add a null check to prevent NullPointerException
        if (chatScreenController != null) {
            chatScreenController.disconnect();
        }
    }

    private void renderHome() {
        if (homeContent == null) {
            try {
                // Load your brand new home.fxml file instead of building it manually
                homeContent = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"));
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback in case the FXML file fails to load
                homeContent = new VBox(new Label("Error loading home screen."));
            }
        }
        // Swap the main center view out for your home layout
        contentBox.getChildren().setAll(homeContent);
        javafx.scene.layout.VBox.setVgrow(homeContent, javafx.scene.layout.Priority.ALWAYS);
        if (contentScroll != null) {
            contentScroll.setPadding(new javafx.geometry.Insets(0));
        }
        currentScreenKey = "home";
    }
    private void setActive(Button active) {
        for (Button b : navButtons()) {
            if (b != null) {
                b.getStyleClass().remove("active");
            }
        }
        if (active != null) {
            active.getStyleClass().add("active");
        }
    }

    private Button[] navButtons() {
        return new Button[]{
                btnHome, btnLostFound, btnVets, btnEmergency, btnFood,
                btnShops, btnMoments, btnDonations, btnShelters, btnAdoption, btnMemes, btnChat
        };
    }

    private void scrollToTop() {
        if (contentScroll != null) {
            contentScroll.setVvalue(0);
        }
    }
}
