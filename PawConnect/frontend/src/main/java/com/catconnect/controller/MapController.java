package com.catconnect.controller;

import com.catconnect.service.ApiClient;
import com.catconnect.service.DeviceLocationService;
import com.catconnect.util.Session;
import com.fasterxml.jackson.databind.JsonNode;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import netscape.javascript.JSObject;

import java.net.URL;
import java.util.Locale;

public class MapController {

    @FXML
    private WebView mapWebView;

    @FXML
    private Label mapStatusLabel;


    private WebEngine webEngine;


    /*
     * Becomes true only after map.html has loaded.
     */
    private boolean mapReady =
            false;


    /*
     * Keep strong Java reference.
     *
     * Otherwise JavaFX may garbage collect
     * the JavaScript bridge.
     */
    private JavaBridge javaBridge;


    /*
     * =========================================================
     * LISTING -> PAW MAP TARGET
     * =========================================================
     *
     * Vet/shop cards can prepare a destination before navigating
     * to the Paw Map. This is deliberately separate from Session's
     * user location: the user's Home location remains the route
     * START, while this target is the route DESTINATION.
     */

    private static MapController activeInstance;

    private static PendingTarget pendingTarget;

    private boolean mapDataReady =
            false;


    private static final class PendingTarget {

        private final double latitude;
        private final double longitude;
        private final String name;
        private final String type;

        private PendingTarget(
                double latitude,
                double longitude,
                String name,
                String type
        ) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name == null || name.isBlank()
                    ? "Destination"
                    : name;
            this.type = type == null || type.isBlank()
                    ? "Destination"
                    : type;
        }
    }


    public static void setPendingTarget(
            double latitude,
            double longitude,
            String name,
            String type
    ) {
        if (
                !Double.isFinite(latitude)
                        ||
                        !Double.isFinite(longitude)
                        ||
                        latitude < -90
                        ||
                        latitude > 90
                        ||
                        longitude < -180
                        ||
                        longitude > 180
                        ||
                        (latitude == 0 && longitude == 0)
        ) {
            return;
        }

        pendingTarget =
                new PendingTarget(
                        latitude,
                        longitude,
                        name,
                        type
                );
    }


    public static void focusPendingTargetIfPossible() {

        MapController controller =
                activeInstance;

        if (controller == null) {
            return;
        }

        Platform.runLater(
                controller::focusPendingTarget
        );
    }


    /*
     * =========================================================
     * INITIALIZATION
     * =========================================================
     */

    @FXML
    public void initialize() {

        activeInstance =
                this;

        if (mapWebView == null) {

            System.err.println(
                    "MapController: mapWebView is null."
            );

            return;
        }


        webEngine =
                mapWebView.getEngine();


        webEngine.setJavaScriptEnabled(
                true
        );


        /*
         * Identify PawConnect instead of pretending to
         * be Chrome when requesting map resources.
         */
        webEngine.setUserAgent(
                "PawConnect/1.0 "
                        + "(JavaFX desktop university project)"
        );


        if (mapStatusLabel != null) {

            mapStatusLabel.setText(
                    "Loading PawConnect map..."
            );
        }


        webEngine.setOnError(event ->

                System.err.println(
                        "WebView Error: "
                                + event.getMessage()
                )
        );


        webEngine
                .getLoadWorker()
                .stateProperty()
                .addListener(

                        (
                                observable,
                                oldState,
                                newState
                        ) -> {

                            if (
                                    newState
                                            == Worker.State.SUCCEEDED
                            ) {

                                handleMapLoaded();

                            } else if (
                                    newState
                                            == Worker.State.FAILED
                            ) {

                                handleMapLoadFailed();
                            }
                        }
                );


        URL mapUrl =
                getClass().getResource(
                        "/map.html"
                );


        if (mapUrl == null) {

            if (mapStatusLabel != null) {

                mapStatusLabel.setText(
                        "map.html could not be found."
                );
            }


            System.err.println(
                    "ERROR: /map.html was not found."
            );


            return;
        }


        webEngine.load(
                mapUrl.toExternalForm()
        );
    }


    /*
     * =========================================================
     * MAP LOADED
     * =========================================================
     */

    private void handleMapLoaded() {

        System.out.println(
                "PawConnect map HTML loaded."
        );


        mapReady =
                true;


        /*
         * Connect JavaScript -> Java.
         */
        connectJavaBridge();


        /*
         * Correct initial JavaFX layout.
         */
        refreshLeaflet();


        /*
         * Restore Home mini-map location immediately.
         */
        refreshFromSession();


        /*
         * Load real vet/shop markers.
         */
        loadMapData();
    }


    private void handleMapLoadFailed() {

        mapReady =
                false;

        mapDataReady =
                false;


        if (mapStatusLabel != null) {

            mapStatusLabel.setText(
                    "Could not load map."
            );
        }


        Throwable error =
                webEngine
                        .getLoadWorker()
                        .getException();


        if (error != null) {

            error.printStackTrace();
        }
    }


    /*
     * =========================================================
     * LOAD VETS + SHOPS
     * =========================================================
     */

    private void loadMapData() {

        if (mapStatusLabel != null) {

            mapStatusLabel.setText(
                    "Loading vets and shops..."
            );
        }


        Thread loaderThread =
                new Thread(() -> {

                    try {

                        JsonNode vets =
                                ApiClient
                                        .get()
                                        .getList(
                                                "/vets"
                                        );


                        JsonNode shops =
                                ApiClient
                                        .get()
                                        .getList(
                                                "/shops"
                                        );


                        Platform.runLater(() -> {

                            if (!mapReady) {

                                return;
                            }


                            try {

                                String script =

                                        "loadPawConnectData("
                                                + vets.toString()
                                                + ","
                                                + shops.toString()
                                                + ");";


                                webEngine.executeScript(
                                        script
                                );


                                mapDataReady =
                                        true;


                                /*
                                 * loadPawConnectData may fit map around
                                 * every vet/shop.
                                 *
                                 * Restore the user location afterwards.
                                 */
                                refreshFromSession();


                                if (mapStatusLabel != null) {

                                    if (
                                            Session
                                                    .hasUserLocation()
                                    ) {

                                        mapStatusLabel.setText(

                                                String.format(
                                                        Locale.US,
                                                        "%s • %.5f, %.5f",
                                                        getLocationDisplayName(),
                                                        Session.getUserLat(),
                                                        Session.getUserLon()
                                                )
                                        );

                                    } else {

                                        mapStatusLabel.setText(
                                                "Map ready • Vets and shops loaded"
                                        );
                                    }
                                }


                                /*
                                 * A vet/shop card may have opened Paw Map.
                                 * Markers now exist, so center on that listing
                                 * and open its popup.
                                 */
                                focusPendingTarget();


                            } catch (Exception e) {

                                e.printStackTrace();


                                if (mapStatusLabel != null) {

                                    mapStatusLabel.setText(
                                            "Map loaded, but marker rendering failed."
                                    );
                                }
                            }
                        });


                    } catch (Exception e) {

                        e.printStackTrace();


                        Platform.runLater(() -> {

                            /*
                             * Even if loading all markers failed, a listing
                             * target can still be shown as a temporary marker.
                             */
                            mapDataReady =
                                    true;

                            if (mapStatusLabel != null) {

                                mapStatusLabel.setText(

                                        "Could not load PawConnect locations: "
                                                + e.getMessage()
                                );
                            }

                            focusPendingTarget();
                        });
                    }

                });


        loaderThread.setName(
                "paw-map-loader"
        );


        loaderThread.setDaemon(
                true
        );


        loaderThread.start();
    }


    /*
     * =========================================================
     * SHARED SESSION -> PAW MAP
     * =========================================================
     */

    public void refreshFromSession() {

        Runnable refreshTask = () -> {

            if (
                    !mapReady
                            ||
                            webEngine == null
            ) {

                System.out.println(
                        "Paw Map not ready yet. "
                                + "Location will restore after load."
                );


                return;
            }


            if (
                    !Session
                            .hasUserLocation()
            ) {

                if (mapStatusLabel != null) {

                    mapStatusLabel.setText(
                            "Map ready • Select your location"
                    );
                }


                return;
            }


            double latitude =
                    Session.getUserLat();


            double longitude =
                    Session.getUserLon();


            String locationName =
                    getLocationDisplayName();


            System.out.println(
                    "Paw Map restoring shared location:"
            );


            System.out.println(
                    locationName
                            + " -> "
                            + latitude
                            + ", "
                            + longitude
            );


            try {

                /*
                 * Move/create user marker.
                 */
                webEngine.executeScript(

                        "setUserLocationFromJava("
                                + latitude
                                + ","
                                + longitude
                                + ");"
                );


                /*
                 * Center the map on the shared location.
                 */
                webEngine.executeScript(

                        "if (window.pawMap) {"
                                +
                                "window.pawMap.setView(["
                                + latitude
                                + ","
                                + longitude
                                + "], 15, {animate:false});"
                                +
                                "}"
                );


                refreshLeaflet();


                if (mapStatusLabel != null) {

                    mapStatusLabel.setText(

                            String.format(
                                    Locale.US,
                                    "%s • %.5f, %.5f",
                                    locationName,
                                    latitude,
                                    longitude
                            )
                    );
                }


            } catch (Exception e) {

                System.err.println(
                        "Could not synchronize Paw Map location."
                );


                e.printStackTrace();
            }
        };


        if (
                Platform
                        .isFxApplicationThread()
        ) {

            refreshTask.run();

        } else {

            Platform.runLater(
                    refreshTask
            );
        }
    }


    /*
     * =========================================================
     * VET / SHOP LISTING -> PAW MAP
     * =========================================================
     */

    private void focusPendingTarget() {

        PendingTarget target =
                pendingTarget;

        if (
                target == null
                        ||
                        !mapReady
                        ||
                        !mapDataReady
                        ||
                        webEngine == null
        ) {
            return;
        }


        String safeName =
                escapeForJavaScript(
                        target.name
                );

        String safeType =
                escapeForJavaScript(
                        target.type
                );


        String script =

                """
                (function() {
                    var map = window.pawMap;

                    if (!map) {
                        return "NO_MAP";
                    }

                    var targetLat = %s;
                    var targetLon = %s;
                    var targetName = '%s';
                    var targetType = '%s';

                    /*
                     * Remove a temporary listing marker left from an
                     * older selection.
                     */
                    if (window.pawSelectedListingMarker) {
                        try {
                            map.removeLayer(
                                window.pawSelectedListingMarker
                            );
                        } catch (ignored) {}

                        window.pawSelectedListingMarker = null;
                    }

                    /*
                     * First try to locate the real vet/shop marker that
                     * loadPawConnectData() already placed on the map.
                     */
                    var found = false;

                    map.eachLayer(function(layer) {
                        if (
                            found
                            ||
                            !layer
                            ||
                            typeof layer.getLatLng !== 'function'
                            ||
                            typeof layer.openPopup !== 'function'
                        ) {
                            return;
                        }

                        try {
                            var point =
                                layer.getLatLng();

                            if (
                                point
                                &&
                                Math.abs(Number(point.lat) - targetLat)
                                    < 0.000001
                                &&
                                Math.abs(Number(point.lng) - targetLon)
                                    < 0.000001
                            ) {
                                layer.openPopup();
                                found = true;
                            }
                        } catch (ignored) {}
                    });

                    map.setView(
                        [targetLat, targetLon],
                        16,
                        { animate: false }
                    );

                    /*
                     * If this is an older/new listing that did not yet
                     * have coordinates stored in the database, Java may
                     * have geocoded it on demand. In that case there is no
                     * permanent marker in loadPawConnectData(), so create
                     * one temporary Paw Map marker.
                     */
                    if (!found && window.L) {

                        var marker =
                            L.marker(
                                [targetLat, targetLon]
                            ).addTo(map);

                        var popup =
                            document.createElement('div');

                        popup.className =
                            'pawPopup';

                        var badge =
                            document.createElement('span');

                        badge.className =
                            'typeBadge';

                        if (targetType === 'Cat Shop') {
                            badge.textContent =
                                '🛒 Cat Shop';
                        } else if (targetType === 'Emergency Vet') {
                            badge.textContent =
                                '🚨 Emergency Vet';
                        } else {
                            badge.textContent =
                                '🏥 Veterinary Clinic';
                        }

                        var heading =
                            document.createElement('h3');

                        heading.textContent =
                            targetName;

                        var button =
                            document.createElement('button');

                        button.className =
                            'routePopupButton';

                        button.textContent =
                            '🚗 Show Route';

                        button.onclick =
                            function(event) {

                                if (event) {
                                    event.stopPropagation();
                                }

                                if (
                                    typeof requestRouteTo
                                        === 'function'
                                ) {
                                    requestRouteTo(
                                        targetLat,
                                        targetLon,
                                        targetName,
                                        targetType
                                    );
                                }
                            };

                        popup.appendChild(
                            badge
                        );

                        popup.appendChild(
                            heading
                        );

                        popup.appendChild(
                            button
                        );

                        marker.bindPopup(
                            popup
                        );

                        marker.openPopup();

                        window.pawSelectedListingMarker =
                            marker;
                    }

                    return found
                        ? "FOUND"
                        : "TEMP";
                })();
                """
                        .formatted(
                                Double.toString(
                                        target.latitude
                                ),
                                Double.toString(
                                        target.longitude
                                ),
                                safeName,
                                safeType
                        );


        try {

            Object result =
                    webEngine.executeScript(
                            script
                    );


            /*
             * The target has now been displayed. Clear it so normal
             * Home -> Paw Map navigation is unaffected later.
             */
            pendingTarget =
                    null;


            refreshLeaflet();


            if (mapStatusLabel != null) {

                mapStatusLabel.setText(
                        "Showing "
                                + target.name
                                + " on Paw Map"
                );
            }


            System.out.println(
                    "Paw Map focused listing: "
                            + target.name
                            + " -> "
                            + target.latitude
                            + ", "
                            + target.longitude
                            + " ("
                            + result
                            + ")"
            );


        } catch (Exception e) {

            System.err.println(
                    "Could not focus Paw Map listing."
            );

            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * JAVASCRIPT BRIDGE
     * =========================================================
     */

    private void connectJavaBridge() {

        try {

            javaBridge =
                    new JavaBridge();


            JSObject window =
                    (JSObject)
                            webEngine.executeScript(
                                    "window"
                            );


            window.setMember(
                    "javaBridge",
                    javaBridge
            );


            System.out.println(
                    "PawConnect map JavaScript bridge connected."
            );


        } catch (Exception e) {

            System.err.println(
                    "Could not connect map Java bridge."
            );


            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * JAVASCRIPT -> JAVA METHODS
     * =========================================================
     */

    public class JavaBridge {

        /*
         * =====================================================
         * MANUAL LOCATION
         * =====================================================
         */

        public void setUserLocation(
                double latitude,
                double longitude
        ) {

            if (
                    !isValidCoordinate(
                            latitude,
                            longitude
                    )
            ) {

                return;
            }


            Session.setUserLocation(
                    latitude,
                    longitude,
                    "Pinned Location"
            );


            System.out.println(
                    "Manual Paw Map location: "
                            + latitude
                            + ", "
                            + longitude
            );


            Platform.runLater(() -> {

                if (mapStatusLabel != null) {

                    mapStatusLabel.setText(

                            String.format(
                                    Locale.US,
                                    "Pinned Location • %.5f, %.5f",
                                    latitude,
                                    longitude
                            )
                    );
                }
            });
        }


        /*
         * =====================================================
         * DEVICE LOCATION
         * =====================================================
         */

        public void requestDeviceLocation() {

            Platform.runLater(() -> {

                if (mapStatusLabel != null) {

                    mapStatusLabel.setText(
                            "Finding your device location..."
                    );
                }


                executeJavaScriptSafely(

                        "if (typeof deviceLocationLoading "
                                + "=== 'function') {"
                                +
                                "deviceLocationLoading(true);"
                                +
                                "}"
                );
            });


            DeviceLocationService.locateAsync(

                    result -> {

                        Platform.runLater(() -> {

                            try {

                                /*
                                 * SUCCESS
                                 */

                                if (
                                        result.success()
                                ) {

                                    double latitude =
                                            result.latitude();


                                    double longitude =
                                            result.longitude();


                                    Session.setUserLocation(
                                            latitude,
                                            longitude,
                                            "Current Location"
                                    );


                                    if (mapStatusLabel != null) {

                                        mapStatusLabel.setText(

                                                String.format(
                                                        Locale.US,
                                                        "Current Location • %.5f, %.5f",
                                                        latitude,
                                                        longitude
                                                )
                                        );
                                    }


                                    executeJavaScriptSafely(

                                            "deviceLocationResult("
                                                    + latitude
                                                    + ","
                                                    + longitude
                                                    + ",true,'');"
                                    );


                                    refreshLeaflet();


                                    return;
                                }


                                /*
                                 * FAILURE
                                 */

                                String message =
                                        escapeForJavaScript(
                                                result.message()
                                        );


                                if (mapStatusLabel != null) {

                                    mapStatusLabel.setText(
                                            "Location unavailable"
                                    );
                                }


                                executeJavaScriptSafely(

                                        "deviceLocationResult("
                                                + "0,"
                                                + "0,"
                                                + "false,'"
                                                + message
                                                + "');"
                                );


                            } catch (Exception e) {

                                e.printStackTrace();


                                if (mapStatusLabel != null) {

                                    mapStatusLabel.setText(
                                            "Could not update map location."
                                    );
                                }
                            }
                        });
                    }
            );
        }


        /*
         * =====================================================
         * ROUTING
         * =====================================================
         *
         * Called by map.html:
         *
         * window.javaBridge.requestRoute(
         *      destinationLat,
         *      destinationLon,
         *      destinationName,
         *      destinationType
         * )
         */

        public void requestRoute(
                double destinationLat,
                double destinationLon,
                String destinationName,
                String destinationType
        ) {

            /*
             * User location is required.
             */

            if (
                    !Session
                            .hasUserLocation()
            ) {

                Platform.runLater(() ->

                        executeJavaScriptSafely(

                                "showRouteError("
                                        +
                                        "'Set your location first. "
                                        +
                                        "Click the map or use "
                                        +
                                        "Use My Location.'"
                                        +
                                        ");"
                        )
                );


                return;
            }


            /*
             * Destination must also be valid.
             */

            if (
                    !isValidCoordinate(
                            destinationLat,
                            destinationLon
                    )
            ) {

                Platform.runLater(() ->

                        executeJavaScriptSafely(

                                "showRouteError("
                                        +
                                        "'Destination coordinates are invalid.'"
                                        +
                                        ");"
                        )
                );


                return;
            }


            final double fromLat =
                    Session.getUserLat();


            final double fromLon =
                    Session.getUserLon();


            final String safeDestinationName =
                    destinationName == null
                            ?
                            "Destination"
                            :
                            destinationName;


            final String safeDestinationType =
                    destinationType == null
                            ?
                            "Destination"
                            :
                            destinationType;


            Platform.runLater(() -> {

                if (mapStatusLabel != null) {

                    mapStatusLabel.setText(

                            "Calculating route to "
                                    + safeDestinationName
                                    + "..."
                    );
                }
            });


            /*
             * Never perform network request on JavaFX thread.
             */

            Thread routeThread =
                    new Thread(() -> {

                        try {

                            /*
                             * Backend controller lives at:
                             *
                             * /api/map/route
                             *
                             * ApiClient's base URL already handles
                             * the application's API prefix in the same
                             * way as /vets and /shops.
                             */

                            String routePath =
                                    String.format(

                                            Locale.US,

                                            "/map/route"
                                                    + "?fromLat=%.8f"
                                                    + "&fromLon=%.8f"
                                                    + "&toLat=%.8f"
                                                    + "&toLon=%.8f",

                                            fromLat,

                                            fromLon,

                                            destinationLat,

                                            destinationLon
                                    );


                            System.out.println(
                                    "Requesting route: "
                                            + routePath
                            );


                            /*
                             * Although this method is named getList(),
                             * ApiClient returns JsonNode and can parse
                             * the object returned by /map/route as well.
                             */

                            JsonNode routeData =
                                    ApiClient
                                            .get()
                                            .getList(
                                                    routePath
                                            );


                            if (routeData == null) {

                                throw new IllegalStateException(
                                        "Route server returned no data."
                                );
                            }


                            boolean success =
                                    routeData
                                            .path(
                                                    "success"
                                            )
                                            .asBoolean(
                                                    false
                                            );


                            if (!success) {

                                String message =
                                        routeData
                                                .path(
                                                        "message"
                                                )
                                                .asText(
                                                        "Could not calculate route."
                                                );


                                throw new IllegalStateException(
                                        message
                                );
                            }


                            double distanceKm =
                                    routeData
                                            .path(
                                                    "distanceKm"
                                            )
                                            .asDouble();


                            double durationMinutes =
                                    routeData
                                            .path(
                                                    "durationMinutes"
                                            )
                                            .asDouble();


                            Platform.runLater(() -> {

                                try {

                                    /*
                                     * Pass backend JSON directly into
                                     * JavaScript as an object literal.
                                     */

                                    String script =

                                            "showRouteResult("
                                                    +
                                                    routeData.toString()
                                                    +
                                                    ","
                                                    +
                                                    destinationLat
                                                    +
                                                    ","
                                                    +
                                                    destinationLon
                                                    +
                                                    ",'"
                                                    +
                                                    escapeForJavaScript(
                                                            safeDestinationName
                                                    )
                                                    +
                                                    "','"
                                                    +
                                                    escapeForJavaScript(
                                                            safeDestinationType
                                                    )
                                                    +
                                                    "');";


                                    webEngine.executeScript(
                                            script
                                    );


                                    if (mapStatusLabel != null) {

                                        mapStatusLabel.setText(

                                                String.format(
                                                        Locale.US,
                                                        "Route • %.1f km • %.0f min",
                                                        distanceKm,
                                                        durationMinutes
                                                )
                                        );
                                    }


                                } catch (Exception e) {

                                    e.printStackTrace();


                                    showRouteFailure(
                                            "Could not display route."
                                    );
                                }
                            });


                        } catch (Exception e) {

                            e.printStackTrace();


                            String message =
                                    e.getMessage();


                            if (
                                    message == null
                                            ||
                                            message.isBlank()
                            ) {

                                message =
                                        "Could not calculate route.";
                            }


                            final String finalMessage =
                                    message;


                            Platform.runLater(() ->

                                    showRouteFailure(
                                            finalMessage
                                    )
                            );
                        }

                    });


            routeThread.setName(
                    "paw-route-loader"
            );


            routeThread.setDaemon(
                    true
            );


            routeThread.start();
        }
    }


    /*
     * =========================================================
     * ROUTE ERROR
     * =========================================================
     */

    private void showRouteFailure(
            String message
    ) {

        String safeMessage =
                escapeForJavaScript(
                        message
                );


        executeJavaScriptSafely(

                "if (typeof showRouteError "
                        + "=== 'function') {"
                        +
                        "showRouteError('"
                        + safeMessage
                        + "');"
                        +
                        "}"
        );


        if (mapStatusLabel != null) {

            mapStatusLabel.setText(
                    "Could not calculate route"
            );
        }
    }


    /*
     * =========================================================
     * LOCATION DISPLAY NAME
     * =========================================================
     */

    private String getLocationDisplayName() {

        String location =
                Session.getSelectedLocation();


        if (
                location == null
                        ||
                        location.isBlank()
                        ||
                        location.equalsIgnoreCase(
                                "All"
                        )
        ) {

            return "Selected Location";
        }


        return location;
    }


    /*
     * =========================================================
     * COORDINATE VALIDATION
     * =========================================================
     */

    private boolean isValidCoordinate(
            double latitude,
            double longitude
    ) {

        return Double.isFinite(
                latitude
        )
                &&
                Double.isFinite(
                        longitude
                )
                &&
                latitude >= -90
                &&
                latitude <= 90
                &&
                longitude >= -180
                &&
                longitude <= 180
                &&
                !(
                        latitude == 0
                                &&
                                longitude == 0
                );
    }


    /*
     * =========================================================
     * SAFE JAVASCRIPT EXECUTION
     * =========================================================
     */

    private void executeJavaScriptSafely(
            String script
    ) {

        if (
                !mapReady
                        ||
                        webEngine == null
                        ||
                        script == null
                        ||
                        script.isBlank()
        ) {

            return;
        }


        try {

            webEngine.executeScript(
                    script
            );


        } catch (Exception e) {

            System.err.println(
                    "Paw Map JavaScript execution failed:"
            );


            System.err.println(
                    script
            );


            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * JAVASCRIPT STRING ESCAPING
     * =========================================================
     */

    private String escapeForJavaScript(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value

                .replace(
                        "\\",
                        "\\\\"
                )

                .replace(
                        "'",
                        "\\'"
                )

                .replace(
                        "\"",
                        "\\\""
                )

                .replace(
                        "\n",
                        " "
                )

                .replace(
                        "\r",
                        " "
                );
    }


    /*
     * =========================================================
     * LEAFLET / JAVAFX REPAINT
     * =========================================================
     *
     * IMPORTANT:
     *
     * We intentionally do NOT call:
     *
     * pawMapTileLayer.redraw()
     *
     * because repeatedly forcing OSM tile requests previously
     * caused the tile server to block PawConnect.
     */

    private void refreshLeaflet() {

        if (
                !mapReady
                        ||
                        webEngine == null
        ) {

            return;
        }


        try {

            webEngine.executeScript(
                    """
                    if (window.pawMap) {
                        window.pawMap.invalidateSize(false);
                    }
                    """
            );


            /*
             * JavaFX may finalize its layout slightly later.
             */

            Platform.runLater(() -> {

                try {

                    webEngine.executeScript(
                            """
                            if (window.pawMap) {
                                window.pawMap.invalidateSize(false);
                            }
                            """
                    );


                } catch (Exception ignored) {
                }
            });


        } catch (Exception e) {

            System.err.println(
                    "Could not resize Leaflet map."
            );


            e.printStackTrace();
        }
    }
}
