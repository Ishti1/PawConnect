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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MainController {

    private static MainController instance;


    /*
     * =========================================================
     * SINGLE INSTANCE
     * =========================================================
     */

    public MainController() {

        instance = this;
    }


    public static MainController getInstance() {

        return instance;
    }


    /*
     * =========================================================
     * FXML COMPONENTS
     * =========================================================
     */

    @FXML
    private Label userLabel;

    @FXML
    private javafx.scene.control.ScrollPane contentScroll;

    @FXML
    private VBox contentBox;

    @FXML
    private VBox sidebar;

    @FXML
    private javafx.scene.control.ScrollPane sidebarScroll;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnHome;

    @FXML
    private Button btnLostFound;

    @FXML
    private Button btnVets;

    @FXML
    private Button btnEmergency;

    @FXML
    private Button btnShops;

    @FXML
    private Button btnMoments;

    @FXML
    private Button btnDonations;

    @FXML
    private Button btnShelters;

    @FXML
    private Button btnAdoption;

    @FXML
    private Button btnCatGame;

    @FXML
    private Button btnCatLibrary;

    @FXML
    private Button btnManageAccount;

    @FXML
    private Button btnChat;


    /*
     * =========================================================
     * SCREEN CACHE
     * =========================================================
     */

    private final Map<String, Parent> screenCache =
            new HashMap<>();

    private final Map<String, Object> controllerCache =
            new HashMap<>();


    private String currentScreenKey;


    /*
     * Home is handled separately because its controller
     * must be retained so the mini-map can refresh from Session.
     */
    private Parent homeContent;

    private HomeController homeScreenController;


    /*
     * Chat controller is retained so the socket can be
     * disconnected when leaving chat.
     */
    private ChatController chatScreenController;


    /*
     * =========================================================
     * INITIALIZATION
     * =========================================================
     */

    @FXML
    public void initialize() {

        instance = this;


        if (Session.getCurrentUser() != null) {

            updateUserLabel();
        }


        /*
         * Sidebar hover behavior.
         */
        if (sidebar != null) {

            sidebar.setOnMouseEntered(
                    e -> expandSidebar()
            );


            sidebar.setOnMouseExited(
                    e -> minimizeSidebar()
            );


            minimizeSidebar();
        }


        setActive(
                btnHome
        );


        renderHome();
    }


    /*
     * =========================================================
     * SIDEBAR
     * =========================================================
     */

    private void expandSidebar() {

        sidebar.setPrefWidth(
                220
        );


        if (userLabel != null) {

            userLabel.setVisible(
                    true
            );

            userLabel.setManaged(
                    true
            );
        }


        for (Button button : navButtons()) {

            if (button != null) {

                setButtonText(
                        button,
                        true
                );
            }
        }


        if (btnLogout != null) {

            setButtonText(
                    btnLogout,
                    true
            );
        }


        if (btnRefresh != null) {

            setButtonText(
                    btnRefresh,
                    true
            );
        }


        if (sidebarScroll != null) {

            sidebarScroll.setVbarPolicy(
                    javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED
            );
        }
    }


    private void minimizeSidebar() {

        sidebar.setPrefWidth(
                80
        );


        if (userLabel != null) {

            userLabel.setVisible(
                    false
            );

            userLabel.setManaged(
                    false
            );
        }


        for (Button button : navButtons()) {

            if (button != null) {

                setButtonText(
                        button,
                        false
                );
            }
        }


        if (btnLogout != null) {

            setButtonText(
                    btnLogout,
                    false
            );
        }


        if (btnRefresh != null) {

            setButtonText(
                    btnRefresh,
                    false
            );
        }


        if (sidebarScroll != null) {

            sidebarScroll.setVbarPolicy(
                    javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
            );
        }
    }


    private void setButtonText(
            Button button,
            boolean expanded
    ) {

        String fullText =
                (String) button
                        .getProperties()
                        .getOrDefault(
                                "fullText",
                                button.getText()
                        );


        button
                .getProperties()
                .putIfAbsent(
                        "fullText",
                        fullText
                );


        if (expanded) {

            button.setText(
                    fullText
            );


            /*
             * Restore CSS styling.
             */
            button.setStyle(
                    ""
            );

        } else {

            int firstSpace =
                    fullText.indexOf(
                            ' '
                    );


            if (firstSpace > 0) {

                button.setText(
                        fullText.substring(
                                0,
                                firstSpace
                        )
                );

            } else if (fullText.length() >= 2) {

                /*
                 * Correctly handles emoji surrogate pairs.
                 */
                button.setText(

                        fullText.substring(
                                0,
                                Character.charCount(
                                        fullText.codePointAt(0)
                                )
                        )
                );

            } else if (!fullText.isEmpty()) {

                button.setText(
                        fullText.substring(
                                0,
                                1
                        )
                );
            }


            button.setStyle(
                    "-fx-alignment: center; "
                            + "-fx-padding: 10 0; "
                            + "-fx-text-overrun: clip;"
            );
        }
    }


    /*
     * =========================================================
     * HOME
     * =========================================================
     */

    @FXML
    private void showHome() {

        setActive(
                btnHome
        );


        showCachedScreen(
                "home",
                null,
                false,
                false
        );
    }


    /*
     * =========================================================
     * LOST & FOUND
     * =========================================================
     */

    @FXML
    private void showLostFound() {

        setActive(
                btnLostFound
        );


        showCachedScreen(
                "lost-found",
                "/fxml/lost_found.fxml",
                false,
                false
        );
    }


    /*
     * =========================================================
     * MOMENTS
     * =========================================================
     */

    @FXML
    private void showMoments() {

        setActive(
                btnMoments
        );


        showCachedScreen(
                "moments",
                "/fxml/moments.fxml",
                false,
                false
        );
    }




    /*
     * =========================================================
     * CAT GAME
     * =========================================================
     */

    @FXML
    private void showCatGame() {

        setActive(
                btnCatGame
        );


        showCachedScreen(
                "catgame",
                "/fxml/catgame.fxml",
                false,
                false
        );
    }


    /*
     * =========================================================
     * CAT LIBRARY
     * =========================================================
     */

    @FXML
    private void showCatLibrary() {

        setActive(
                btnCatLibrary
        );


        showCachedScreen(
                "cat-library",
                "/fxml/cat_library.fxml",
                false,
                false
        );
    }


    /*
     * =========================================================
     * MANAGE ACCOUNT
     * =========================================================
     */

    @FXML
    private void showManageAccount() {

        setActive(
                btnManageAccount
        );


        showCachedScreen(
                "manage-account",
                "/fxml/manage_account.fxml",
                false,
                true
        );
    }


    /*
     * =========================================================
     * ADOPTION
     * =========================================================
     */

    @FXML
    private void showAdoption() {

        setActive(
                btnAdoption
        );


        showCachedScreen(
                "adoption",
                "/fxml/adoption.fxml",
                false,
                false
        );
    }


    /*
     * =========================================================
     * VETS
     * =========================================================
     */

    @FXML
    private void showVets() {

        setActive(
                btnVets
        );


        showCachedScreen(

                "vets",

                "/fxml/vets.fxml",

                false,

                false,

                controller -> {

                    if (controller instanceof VetsController vetsController) {

                        vetsController.setEmergencyOnly(
                                false
                        );
                    }
                }
        );
    }


    /*
     * =========================================================
     * EMERGENCY VETS
     * =========================================================
     */

    @FXML
    private void showEmergency() {

        setActive(
                btnEmergency
        );


        showCachedScreen(

                "vets-emergency",

                "/fxml/vets.fxml",

                false,

                false,

                controller -> {

                    if (controller instanceof VetsController vetsController) {

                        vetsController.setEmergencyOnly(
                                true
                        );
                    }
                }
        );
    }


    /*
     * =========================================================
     * SHOPS
     * =========================================================
     */

    @FXML
    private void showShops() {

        setActive(
                btnShops
        );


        showCachedScreen(
                "shops",
                "/fxml/shops.fxml",
                false,
                false
        );
    }


    /*
     * =========================================================
     * PAW MAP
     * =========================================================
     *
     * Paw Map is intentionally NOT displayed in the sidebar.
     *
     * It is opened from the Home mini-map.
     */

    private void showMap() {

        /*
         * No sidebar button should appear active.
         */
        setActive(
                null
        );


        showCachedScreen(

                "map",

                "/fxml/map.fxml",

                false,

                false,

                controller -> {

                    /*
                     * We intentionally invoke this through a
                     * generic helper for now.
                     *
                     * After MapController receives the
                     * refreshFromSession() method, this causes
                     * every Paw Map opening to synchronize with
                     * the latest Home location.
                     */

                    refreshLocationController(
                            controller
                    );
                }
        );
    }


    /*
     * =========================================================
     * DONATIONS
     * =========================================================
     */

    @FXML
    private void showDonations() {

        setActive(
                btnDonations
        );


        showCachedScreen(
                "donations",
                "/fxml/donations.fxml",
                false,
                false
        );
    }


    /*
     * =========================================================
     * SHELTERS
     * =========================================================
     */

    @FXML
    private void showShelters() {

        setActive(
                btnShelters
        );


        showCachedScreen(
                "shelters",
                "/fxml/shelters.fxml",
                false,
                false
        );
    }


    /*
     * =========================================================
     * CHAT
     * =========================================================
     */

    @FXML
    private void showChat() {

        setActive(
                btnChat
        );


        showCachedScreen(
                "chat",
                "/fxml/chat.fxml",
                true,
                true
        );
    }


    /*
     * =========================================================
     * PUBLIC NAVIGATION
     * =========================================================
     *
     * HTML WebViews call this through their controller bridge.
     */

    public void navigateTo(
            String screen
    ) {

        Platform.runLater(() -> {

            switch (screen) {

                case "vets" -> showVets();

                case "map" -> showMap();

                case "adoption" -> showAdoption();

                case "lost-found" -> showLostFound();

                case "shops" -> showShops();

                case "chat" -> showChat();

                case "moments" -> showMoments();

                case "donations" -> showDonations();

                case "shelters" -> showShelters();

                case "catgame", "cat-game", "memory", "memory-match", "game" ->
                        showCatGame();

                case "cat-library", "library" ->
                        showCatLibrary();

                case "manage-account" ->
                        showManageAccount();

                default ->
                        showHome();
            }
        });
    }


    /*
     * =========================================================
     * USER LABEL
     * =========================================================
     */

    public void updateUserLabel() {

        if (Session.getCurrentUser() != null
                && userLabel != null) {

            userLabel.setText(

                    "Hi, "
                            + Session
                            .getCurrentUser()
                            .getDisplayName()
            );
        }
    }


    /*
     * =========================================================
     * REFRESH BUTTON
     * =========================================================
     */

    @FXML
    private void onRefresh() {

        /*
         * Clear cached screens.
         */
        screenCache.clear();

        controllerCache.clear();


        /*
         * Home has its own cache.
         */
        homeContent =
                null;

        homeScreenController =
                null;


        /*
         * Chat controller belongs to cached screen.
         */
        chatScreenController =
                null;


        Button activeButton =
                null;


        for (Button button : navButtons()) {

            if (button != null
                    && button
                    .getStyleClass()
                    .contains("active")) {

                activeButton =
                        button;

                break;
            }
        }


        if (activeButton != null) {

            activeButton.fire();

        } else {

            /*
             * Paw Map has no active sidebar button.
             *
             * Refreshing while on it falls back Home.
             */
            showHome();
        }
    }


    /*
     * =========================================================
     * LOGOUT
     * =========================================================
     */

    @FXML
    private void onLogout() {

        disconnectChat();


        screenCache.clear();

        controllerCache.clear();


        currentScreenKey =
                null;


        homeContent =
                null;

        homeScreenController =
                null;

        chatScreenController =
                null;


        /*
         * Session.clear() also clears the shared
         * location so another user never inherits it.
         */
        Session.clear();


        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/login.fxml"
                            )
                    );


            Parent root =
                    loader.load();


            javafx.stage.Stage stage =
                    (javafx.stage.Stage)
                            contentBox
                                    .getScene()
                                    .getWindow();


            stage.setScene(
                    new javafx.scene.Scene(
                            root,
                            1100,
                            700
                    )
            );


        } catch (Exception e) {

            UiHelper.showError(
                    e.getMessage()
            );
        }
    }


    /*
     * =========================================================
     * SCREEN CACHE
     * =========================================================
     */

    private void showCachedScreen(
            String key,
            String fxmlPath,
            boolean isChat,
            boolean scrollTop
    ) {

        showCachedScreen(
                key,
                fxmlPath,
                isChat,
                scrollTop,
                null
        );
    }


    private void showCachedScreen(
            String key,
            String fxmlPath,
            boolean isChat,
            boolean scrollTop,
            Consumer<Object> controllerSetup
    ) {

        /*
         * Home is special because it uses HomeController
         * to synchronize its WebView.
         */
        if ("home".equals(key)) {

            renderHome();

            currentScreenKey =
                    key;

            return;
        }


        /*
         * Disconnect WebSocket chat when leaving chat.
         */
        if (!isChat) {

            disconnectChat();
        }


        boolean returningToSame =
                key.equals(
                        currentScreenKey
                );


        Parent root =
                screenCache.get(
                        key
                );


        /*
         * =====================================================
         * LOAD SCREEN FOR FIRST TIME
         * =====================================================
         */

        if (root == null
                && fxmlPath != null) {

            try {

                FXMLLoader loader =
                        new FXMLLoader(
                                getClass().getResource(
                                        fxmlPath
                                )
                        );


                root =
                        loader.load();


                Object controller =
                        loader.getController();


                screenCache.put(
                        key,
                        root
                );


                controllerCache.put(
                        key,
                        controller
                );


                if (isChat
                        && controller instanceof ChatController chatController) {

                    chatScreenController =
                            chatController;
                }


                /*
                 * Run screen-specific configuration.
                 *
                 * This also runs the first Paw Map
                 * synchronization.
                 */
                if (controllerSetup != null) {

                    controllerSetup.accept(
                            controller
                    );
                }


            } catch (Exception e) {

                e.printStackTrace();


                UiHelper.showError(

                        "Failed to load screen: "
                                + e.getMessage()
                );


                return;
            }


        } else {

            /*
             * =================================================
             * SCREEN ALREADY CACHED
             * =================================================
             */

            Object cachedController =
                    controllerCache.get(
                            key
                    );


            if (isChat
                    && cachedController instanceof ChatController chatController) {

                chatScreenController =
                        chatController;
            }


            /*
             * Existing list controllers refresh their API data.
             */
            if (cachedController
                    instanceof BaseListController listController) {

                listController.refreshData();
            }


            /*
             * VERY IMPORTANT:
             *
             * controllerSetup also runs when a cached screen
             * is reopened.
             *
             * This is how Paw Map gets the newest Session
             * coordinates instead of showing its old marker.
             */
            if (controllerSetup != null
                    && cachedController != null) {

                controllerSetup.accept(
                        cachedController
                );
            }
        }


        if (root == null) {

            return;
        }


        /*
         * Display the screen.
         */
        if (!contentBox
                .getChildren()
                .contains(root)) {

            contentBox
                    .getChildren()
                    .setAll(
                            root
                    );


            VBox.setVgrow(
                    root,
                    Priority.ALWAYS
            );
        }


        /*
         * =====================================================
         * CONTENT AREA STYLE
         * =====================================================
         */

        if (contentScroll != null) {

            if ("moments".equals(key)) {

                contentScroll.setPadding(
                        new javafx.geometry.Insets(
                                0,
                                0,
                                0,
                                0
                        )
                );


                contentScroll.setStyle(
                        "-fx-background-color: #ff6b6b;"
                );


                contentBox.setStyle(
                        "-fx-background-color: #ff6b6b;"
                );


                contentBox.setPadding(
                        new javafx.geometry.Insets(
                                0,
                                0,
                                0,
                                0
                        )
                );


            } else {

                contentScroll.setPadding(
                        new javafx.geometry.Insets(
                                30,
                                40,
                                30,
                                40
                        )
                );


                contentScroll.setStyle(
                        "-fx-background-color: transparent;"
                );


                contentBox.setStyle(
                        "-fx-background-color: transparent;"
                );


                contentBox.setPadding(
                        new javafx.geometry.Insets(
                                0,
                                0,
                                0,
                                0
                        )
                );
            }
        }


        currentScreenKey =
                key;


        if (scrollTop
                && !returningToSame) {

            scrollToTop();
        }
    }


    /*
     * =========================================================
     * LOCATION-AWARE CONTROLLER REFRESH
     * =========================================================
     *
     * This avoids making MainController dependent on a
     * refreshFromSession() method before MapController is
     * replaced in the next step.
     *
     * Once MapController has refreshFromSession(), it will
     * automatically be called here.
     */

    private void refreshLocationController(
            Object controller
    ) {

        if (controller == null) {

            return;
        }


        try {

            java.lang.reflect.Method method =
                    controller
                            .getClass()
                            .getMethod(
                                    "refreshFromSession"
                            );


            method.invoke(
                    controller
            );


        } catch (NoSuchMethodException e) {

            /*
             * This is expected until MapController.java
             * receives its synchronization update.
             */
            System.out.println(

                    controller
                            .getClass()
                            .getSimpleName()

                            + " does not yet implement "
                            + "refreshFromSession()."
            );


        } catch (Exception e) {

            System.err.println(
                    "Could not refresh location-aware screen."
            );


            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * DIRECT CHAT
     * =========================================================
     */

    public void openDirectChat(
            Long targetUserId,
            String userName
    ) {

        setActive(
                btnChat
        );


        showCachedScreen(

                "chat",

                "/fxml/chat.fxml",

                true,

                true,

                controller -> {

                    if (controller
                            instanceof ChatController chatController) {

                        chatController.openChatWithUser(
                                targetUserId,
                                userName
                        );
                    }
                }
        );
    }


    /*
     * =========================================================
     * READ-ONLY GENERIC SCREEN
     * =========================================================
     */

    private void showReadOnly(
            String key,
            String title,
            String path,
            Consumer<JsonNode> renderer
    ) {

        Parent root =
                screenCache.get(
                        key
                );


        VBox box;


        if (root instanceof VBox existingVBox) {

            box =
                    existingVBox;

        } else {

            box =
                    new VBox(
                            16
                    );


            box.getChildren().add(

                    UiHelper.sectionTitle(
                            title
                    )
            );


            Label loading =
                    new Label(
                            "Loading..."
                    );


            loading.setId(
                    "loading"
            );


            box.getChildren().add(
                    loading
            );


            screenCache.put(
                    key,
                    box
            );
        }


        contentBox
                .getChildren()
                .setAll(
                        box
                );


        currentScreenKey =
                key;


        scrollToTop();


        /*
         * Always reload API data when read-only screen opens.
         */
        new Thread(() -> {

            try {

                JsonNode data =
                        ApiClient
                                .get()
                                .getList(
                                        path
                                );


                Platform.runLater(() -> {

                    box.getChildren().removeIf(

                            node ->

                                    "loading".equals(
                                            node.getId()
                                    )

                                            ||

                                            (
                                                    node instanceof Label label

                                                            &&

                                                            label
                                                                    .getText()
                                                                    .startsWith(
                                                                            "Error:"
                                                                    )
                                            )
                    );


                    /*
                     * Keep the title but remove old cards.
                     */
                    while (box
                            .getChildren()
                            .size() > 1) {

                        box
                                .getChildren()
                                .remove(
                                        1
                                );
                    }


                    renderer.accept(
                            data
                    );
                });


            } catch (Exception e) {

                Platform.runLater(() ->

                        box
                                .getChildren()
                                .add(

                                        new Label(
                                                "Error: "
                                                        + e.getMessage()
                                        )
                                )
                );
            }

        }, "pawconnect-readonly-loader").start();
    }


    /*
     * =========================================================
     * LEGACY VET RENDERER
     * =========================================================
     */

    private void renderVets(
            JsonNode data
    ) {

        VBox box =
                (VBox)
                        contentBox
                                .getChildren()
                                .get(0);


        for (JsonNode vet : data) {

            String emergency =
                    vet
                            .path(
                                    "emergency"
                            )
                            .asBoolean()

                            ? " [EMERGENCY]"

                            : "";


            box.getChildren().add(

                    addressCard(

                            vet
                                    .path("name")
                                    .asText()
                                    + emergency,

                            vet
                                    .path("mapLink")
                                    .asText(""),

                            "★ "
                                    + vet
                                    .path("rating")
                                    .asText()

                                    + " | "

                                    + vet
                                    .path("address")
                                    .asText()

                                    + "\n📞 "

                                    + vet
                                    .path("phone")
                                    .asText()

                                    + " | "

                                    + vet
                                    .path("openHours")
                                    .asText()
                    )
            );
        }
    }


    /*
     * =========================================================
     * LEGACY SHOP RENDERER
     * =========================================================
     */

    private void renderShops(
            JsonNode data
    ) {

        VBox box =
                (VBox)
                        contentBox
                                .getChildren()
                                .get(0);


        for (JsonNode shop : data) {

            box.getChildren().add(

                    addressCard(

                            shop
                                    .path("name")
                                    .asText(),

                            shop
                                    .path("mapLink")
                                    .asText(""),

                            shop
                                    .path("address")
                                    .asText()

                                    + " | ★ "

                                    + shop
                                    .path("rating")
                                    .asText()

                                    + " | 📞 "

                                    + shop
                                    .path("phone")
                                    .asText()
                    )
            );
        }
    }


    /*
     * =========================================================
     * LEGACY SHELTER RENDERER
     * =========================================================
     */

    private void renderShelters(
            JsonNode data
    ) {

        VBox box =
                (VBox)
                        contentBox
                                .getChildren()
                                .get(0);


        for (JsonNode shelter : data) {

            box.getChildren().add(

                    addressCard(

                            shelter
                                    .path("name")
                                    .asText(),

                            shelter
                                    .path("mapLink")
                                    .asText(""),

                            shelter
                                    .path("description")
                                    .asText()

                                    + "\n📍 "

                                    + shelter
                                    .path("address")
                                    .asText()

                                    + " | Capacity: "

                                    + shelter
                                    .path("capacity")
                                    .asText()
                    )
            );
        }
    }


    /*
     * =========================================================
     * GENERIC ADDRESS CARD
     * =========================================================
     */

    private VBox addressCard(
            String title,
            String mapLink,
            String detail
    ) {

        VBox card =
                UiHelper.card(
                        title,
                        detail
                );


        if (mapLink != null
                && !mapLink.isBlank()) {

            Button mapButton =
                    new Button(
                            "Address Link"
                    );


            mapButton
                    .getStyleClass()
                    .add(
                            "secondary-button"
                    );


            String url =
                    mapLink.trim();


            mapButton.setOnAction(

                    event -> openAddressLink(
                            url
                    )
            );


            card
                    .getChildren()
                    .add(

                            new HBox(
                                    mapButton
                            )
                    );
        }


        return card;
    }


    private void openAddressLink(
            String mapLink
    ) {

        try {

            java.awt.Desktop
                    .getDesktop()
                    .browse(

                            java.net.URI.create(
                                    mapLink.trim()
                            )
                    );


        } catch (Exception exception) {

            UiHelper.showError(

                    "Could not open address link. "
                            + "Paste a full URL (https://...)."
            );
        }
    }


    /*
     * =========================================================
     * CHAT DISCONNECT
     * =========================================================
     */

    private void disconnectChat() {

        if (chatScreenController != null) {

            chatScreenController.disconnect();
        }
    }


    /*
     * =========================================================
     * HOME RENDERING
     * =========================================================
     *
     * IMPORTANT CHANGE:
     *
     * We use a real FXMLLoader instance instead of:
     *
     * FXMLLoader.load(...)
     *
     * This allows us to keep HomeController.
     */

    private void renderHome() {

        boolean firstLoad =
                false;


        if (homeContent == null) {

            try {

                FXMLLoader loader =
                        new FXMLLoader(
                                getClass().getResource(
                                        "/fxml/home.fxml"
                                )
                        );


                homeContent =
                        loader.load();


                /*
                 * Keep the controller.
                 */
                homeScreenController =
                        loader.getController();


                firstLoad =
                        true;


            } catch (Exception e) {

                e.printStackTrace();


                homeContent =
                        new VBox(

                                new Label(
                                        "Error loading home screen."
                                )
                        );


                homeScreenController =
                        null;
            }
        }


        /*
         * HomeController.initialize() already loads the
         * current Session on its first creation.
         *
         * When returning later from Paw Map, refresh it.
         */
        if (!firstLoad
                && homeScreenController != null) {

            homeScreenController
                    .refreshFromSession();
        }


        contentBox
                .getChildren()
                .setAll(
                        homeContent
                );


        VBox.setVgrow(
                homeContent,
                Priority.ALWAYS
        );


        if (contentScroll != null) {

            contentScroll.setPadding(

                    new javafx.geometry.Insets(
                            0
                    )
            );


            /*
             * Restore Home background in case previous
             * screen was Moments.
             */
            contentScroll.setStyle(
                    "-fx-background-color: transparent;"
            );


            contentBox.setStyle(
                    "-fx-background-color: transparent;"
            );


            contentBox.setPadding(

                    new javafx.geometry.Insets(
                            0
                    )
            );
        }


        currentScreenKey =
                "home";
    }


    /*
     * =========================================================
     * ACTIVE SIDEBAR BUTTON
     * =========================================================
     */

    private void setActive(
            Button active
    ) {

        for (Button button : navButtons()) {

            if (button != null) {

                button
                        .getStyleClass()
                        .remove(
                                "active"
                        );
            }
        }


        if (active != null) {

            active
                    .getStyleClass()
                    .add(
                            "active"
                    );
        }
    }


    /*
     * Paw Map is intentionally NOT included.
     */
    private Button[] navButtons() {

        return new Button[]{

                btnHome,

                btnLostFound,

                btnVets,

                btnEmergency,

                btnShops,

                btnMoments,

                btnDonations,

                btnShelters,

                btnAdoption,

                btnCatGame,

                btnCatLibrary,

                btnManageAccount,

                btnChat
        };
    }


    /*
     * =========================================================
     * SCROLL TO TOP
     * =========================================================
     */

    private void scrollToTop() {

        if (contentScroll != null) {

            contentScroll.setVvalue(
                    0
            );
        }
    }
}