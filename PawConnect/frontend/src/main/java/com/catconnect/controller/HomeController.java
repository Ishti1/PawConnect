package com.catconnect.controller;

import com.catconnect.service.DeviceLocationService;
import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class HomeController {

    @FXML
    private WebView homeWebView;

    private WebEngine webEngine;


    /*
     * =========================================================
     * INITIALIZATION
     * =========================================================
     */

    @FXML
    public void initialize() {

        if (homeWebView == null) {

            System.err.println(
                    "HomeController: homeWebView is null."
            );

            return;
        }


        webEngine =
                homeWebView.getEngine();


        webEngine.setJavaScriptEnabled(true);


        /*
         * Some web services behave better when JavaFX WebView
         * identifies itself using a normal browser user-agent.
         */
        webEngine.setUserAgent(
                "PawConnect/1.0 (+https://github.com/Ishti1/PawConnect)"
        );


        /*
         * JavaScript -> Java communication.
         *
         * home.html uses alert(...) as a lightweight bridge.
         */
        webEngine.setOnAlert(event -> {

            String data =
                    event.getData();


            System.out.println(
                    "WebEngine Alert Intercepted: "
                            + data
            );


            if (data == null
                    || data.isBlank()) {

                return;
            }


            handleJavascriptCommand(
                    data
            );
        });


        /*
         * Load homepage using the current shared Session.
         */
        updateHomeUI();
    }


    /*
     * =========================================================
     * JAVASCRIPT COMMAND ROUTER
     * =========================================================
     */

    private void handleJavascriptCommand(
            String data
    ) {

        /*
         * -----------------------------------------------------
         * SCREEN NAVIGATION
         *
         * Example:
         *
         * alert("navigate:vets")
         * alert("navigate:map")
         * -----------------------------------------------------
         */

        if (data.startsWith("navigate:")) {

            String screen =
                    data.substring(
                            "navigate:".length()
                    );


            Platform.runLater(() -> {

                MainController main =
                        MainController.getInstance();


                if (main != null) {

                    main.navigateTo(
                            screen
                    );

                } else {

                    System.err.println(
                            "MainController instance is null "
                                    + "during navigateTo("
                                    + screen
                                    + ")"
                    );
                }
            });


            return;
        }


        /*
         * -----------------------------------------------------
         * LOCATION WITH REAL COORDINATES
         *
         * Format:
         *
         * locationSelected:lat,lon,name
         *
         * Example:
         *
         * locationSelected:
         * 23.7465,90.3760,Dhanmondi
         * -----------------------------------------------------
         */

        if (data.startsWith("locationSelected:")) {

            saveSelectedLocation(
                    data
            );

            return;
        }


        /*
         * -----------------------------------------------------
         * LEGACY TEXT-ONLY LOCATION
         *
         * Kept so older homepage code will not break.
         * -----------------------------------------------------
         */

        if (data.startsWith("selectLocation:")) {

            String location =
                    data.substring(
                            "selectLocation:".length()
                    );


            Platform.runLater(() -> {

                Session.setSelectedLocation(
                        location
                );


                UiHelper.showInfo(
                        "Location selected: "
                                + location
                );
            });


            return;
        }


        /*
         * -----------------------------------------------------
         * OPEN FULL PAW MAP
         * -----------------------------------------------------
         */

        if (data.equals("openPawMap")) {

            Platform.runLater(() -> {

                MainController main =
                        MainController.getInstance();


                if (main != null) {

                    main.navigateTo(
                            "map"
                    );
                }
            });


            return;
        }


        /*
         * -----------------------------------------------------
         * AUTOMATIC DEVICE LOCATION
         * -----------------------------------------------------
         */

        if (data.equals("getExactLocation")) {

            requestExactDeviceLocation();
        }
    }


    /*
     * =========================================================
     * SAVE LOCATION FROM HOMEPAGE
     * =========================================================
     */

    private void saveSelectedLocation(
            String data
    ) {

        try {

            String raw =
                    data.substring(
                            "locationSelected:".length()
                    );


            String[] parts =
                    raw.split(
                            ",",
                            3
                    );


            if (parts.length < 2) {

                System.err.println(
                        "Invalid locationSelected message: "
                                + data
                );

                return;
            }


            double latitude =
                    Double.parseDouble(
                            parts[0]
                    );


            double longitude =
                    Double.parseDouble(
                            parts[1]
                    );


            String locationName =
                    "Selected Location";


            if (parts.length >= 3
                    && !parts[2].isBlank()) {

                locationName =
                        URLDecoder.decode(
                                parts[2],
                                StandardCharsets.UTF_8
                        );
            }


            final String finalLocationName =
                    locationName;


            Platform.runLater(() -> {

                /*
                 * IMPORTANT:
                 *
                 * This is now the single shared location
                 * used by:
                 *
                 * Home mini-map
                 * Paw Map
                 * Future routing
                 * Nearby-vet distance
                 * Nearby-shop distance
                 */

                Session.setUserLocation(
                        latitude,
                        longitude,
                        finalLocationName
                );


                System.out.println(
                        "HOME LOCATION SAVED"
                );

                System.out.println(
                        "Name: "
                                + finalLocationName
                );

                System.out.println(
                        "Latitude: "
                                + latitude
                );

                System.out.println(
                        "Longitude: "
                                + longitude
                );
            });


        } catch (Exception e) {

            System.err.println(
                    "Could not process homepage location:"
            );

            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * AUTOMATIC WINDOWS LOCATION
     * =========================================================
     */

    private void requestExactDeviceLocation() {

        System.out.println(
                "Homepage requested device location..."
        );


        /*
         * Wi-Fi information is kept as an optional fallback
         * because some versions of home.html still accept
         * BSSIDs in exactLocationCallback().
         */
        Thread wifiThread =
                new Thread(() -> {

                    String bssids =
                            fetchWifiBssids();


                    requestWindowsLocation(
                            bssids
                    );
                });


        wifiThread.setName(
                "pawconnect-home-location-request"
        );

        wifiThread.setDaemon(true);

        wifiThread.start();
    }


    /*
     * =========================================================
     * WINDOWS LOCATION SERVICE
     * =========================================================
     */

    private void requestWindowsLocation(
            String bssids
    ) {

        DeviceLocationService.locateAsync(

                result -> {

                    Platform.runLater(() -> {

                        if (webEngine == null) {

                            return;
                        }


                        /*
                         * SUCCESS
                         */
                        if (result.success()) {

                            double latitude =
                                    result.latitude();


                            double longitude =
                                    result.longitude();


                            /*
                             * Save into the exact same Session
                             * used by Paw Map.
                             */

                            Session.setUserLocation(
                                    latitude,
                                    longitude,
                                    "Current Location"
                            );


                            System.out.println(
                                    "HOME DEVICE LOCATION FOUND"
                            );

                            System.out.println(
                                    latitude
                                            + ", "
                                            + longitude
                            );


                            /*
                             * Send result back to home.html.
                             *
                             * home.html can now display the
                             * same location on its mini-map.
                             */

                            executeJavascriptSafely(

                                    "exactLocationCallback("
                                            + latitude
                                            + ","
                                            + longitude
                                            + ",'"
                                            + escapeJavascript(
                                            bssids
                                    )
                                            + "');"
                            );


                            return;
                        }


                        /*
                         * FAILURE
                         *
                         * Let home.html perform whatever fallback
                         * behavior it already provides.
                         */

                        System.err.println(
                                "Device location unavailable: "
                                        + result.message()
                        );


                        executeJavascriptSafely(

                                "exactLocationCallback("
                                        + "null,"
                                        + "null,'"
                                        + escapeJavascript(
                                        bssids
                                )
                                        + "');"
                        );
                    });
                }
        );
    }


    /*
     * =========================================================
     * OPTIONAL WIFI INFORMATION
     * =========================================================
     */

    private String fetchWifiBssids() {

        List<String> macAddresses =
                new ArrayList<>();


        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "netsh",
                            "wlan",
                            "show",
                            "networks",
                            "mode=bssid"
                    );


            processBuilder.redirectErrorStream(
                    true
            );


            Process process =
                    processBuilder.start();


            String output =
                    new String(
                            process
                                    .getInputStream()
                                    .readAllBytes(),

                            StandardCharsets.UTF_8
                    );


            process.waitFor();


            for (String line :
                    output.split("\\R")) {

                String trimmed =
                        line.trim();


                /*
                 * Expected example:
                 *
                 * BSSID 1 : AA:BB:CC:DD:EE:FF
                 */

                if (trimmed.startsWith("BSSID")) {

                    int separator =
                            trimmed.indexOf(" : ");


                    if (separator >= 0) {

                        String mac =
                                trimmed.substring(
                                                separator + 3
                                        )
                                        .trim();


                        if (!mac.isBlank()) {

                            macAddresses.add(
                                    mac
                            );
                        }
                    }
                }
            }


        } catch (Exception e) {

            /*
             * Wi-Fi information is optional.
             *
             * Do not fail location detection just because
             * netsh was unavailable.
             */

            System.out.println(
                    "Wi-Fi BSSID lookup unavailable."
            );
        }


        return String.join(
                ",",
                macAddresses
        );
    }


    /*
     * =========================================================
     * REFRESH HOME FROM SHARED SESSION
     * =========================================================
     *
     * MainController will call this whenever the user returns
     * to Home.
     *
     * Example:
     *
     * User moves marker on Paw Map
     *          ↓
     * Session changes
     *          ↓
     * User returns Home
     *          ↓
     * refreshFromSession()
     *          ↓
     * mini-map shows same coordinates
     */

    public void refreshFromSession() {

        Runnable refreshTask = () -> {

            System.out.println(
                    "Refreshing Home from shared Session"
            );


            System.out.println(
                    "Location: "
                            + Session.getSelectedLocation()
            );


            System.out.println(
                    "Coordinates: "
                            + Session.getUserLat()
                            + ", "
                            + Session.getUserLon()
            );


            updateHomeUI();
        };


        if (Platform.isFxApplicationThread()) {

            refreshTask.run();

        } else {

            Platform.runLater(
                    refreshTask
            );
        }
    }


    /*
     * =========================================================
     * LOAD HOME.HTML
     * =========================================================
     */

    private void updateHomeUI() {

        if (webEngine == null) {

            return;
        }


        double userLat =
                Session.getUserLat();


        double userLon =
                Session.getUserLon();


        String selectedLocation =
                Session.getSelectedLocation();


        if (selectedLocation == null) {

            selectedLocation =
                    "All";
        }


        try {

            java.net.URL htmlUrl =
                    getClass().getResource(
                            "/html/home.html"
                    );


            if (htmlUrl == null) {

                System.err.println(
                        "Could not find /html/home.html"
                );

                return;
            }


            String html =
                    new String(
                            htmlUrl
                                    .openStream()
                                    .readAllBytes(),

                            StandardCharsets.UTF_8
                    );


            /*
             * -------------------------------------------------
             * RELATIVE IMAGE SUPPORT
             * -------------------------------------------------
             */

            java.net.URL baseUrl =
                    getClass().getResource(
                            "/images/"
                    );


            if (baseUrl != null) {

                html =
                        html.replace(
                                "<head>",

                                "<head>\n"
                                        + "<base href=\""
                                        + baseUrl.toExternalForm()
                                        + "\">"
                        );
            }


            /*
             * -------------------------------------------------
             * TEMPLATE VALUES
             * -------------------------------------------------
             */

            String locationForHtml =

                    selectedLocation.equals("All")

                            ? ""

                            : selectedLocation;


            /*
             * Map visibility should depend on coordinates,
             * NOT the text label.
             *
             * This is important for synchronization.
             */

            boolean mapVisible =
                    Session.hasUserLocation();


            html =
                    html.replaceAll(

                            "\\$\\{\\s*USER_LAT\\s*\\}",

                            Matcher.quoteReplacement(
                                    String.valueOf(
                                            userLat
                                    )
                            )
                    );


            html =
                    html.replaceAll(

                            "\\$\\{\\s*USER_LON\\s*\\}",

                            Matcher.quoteReplacement(
                                    String.valueOf(
                                            userLon
                                    )
                            )
                    );


            html =
                    html.replaceAll(

                            "\\$\\{\\s*LOCATION\\s*\\}",

                            Matcher.quoteReplacement(
                                    locationForHtml
                            )
                    );


            html =
                    html.replaceAll(

                            "\\$\\{\\s*IS_MAP_VISIBLE\\s*\\}",

                            mapVisible
                                    ? "true"
                                    : "false"
                    );


            /*
             * -------------------------------------------------
             * TEMP HTML FILE
             * -------------------------------------------------
             *
             * JavaFX WebView can occasionally behave badly
             * with UTF-8 content loaded directly.
             *
             * Keep your existing temp-file solution.
             */

            java.io.File tempHtml =
                    java.io.File.createTempFile(
                            "pawconnect_home_",
                            ".html"
                    );


            tempHtml.deleteOnExit();


            java.nio.file.Files.writeString(

                    tempHtml.toPath(),

                    html,

                    StandardCharsets.UTF_8
            );


            webEngine.load(
                    tempHtml
                            .toURI()
                            .toString()
            );


        } catch (Exception e) {

            System.err.println(
                    "Failed to load PawConnect homepage."
            );

            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * SAFE JAVASCRIPT EXECUTION
     * =========================================================
     */

    private void executeJavascriptSafely(
            String script
    ) {

        if (webEngine == null
                || script == null
                || script.isBlank()) {

            return;
        }


        try {

            webEngine.executeScript(
                    script
            );


        } catch (Exception e) {

            System.err.println(
                    "Homepage JavaScript execution failed:"
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

    private String escapeJavascript(
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
                        "\r",
                        ""
                )

                .replace(
                        "\n",
                        ""
                );
    }
}