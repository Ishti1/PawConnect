package com.catconnect.controller;

import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import com.fasterxml.jackson.databind.JsonNode;
import com.catconnect.service.ApiClient;
import java.nio.charset.StandardCharsets;

public class HomeController {

    @FXML
    private WebView homeWebView;

    @FXML
    private VBox notificationsBox;

    private WebEngine webEngine;
    
    @FXML
    public void initialize() {
        if (homeWebView != null) {
            webEngine = homeWebView.getEngine();
            webEngine.setJavaScriptEnabled(true);
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            
            // Reliable alert-based bridge: JS calls alert("cmd:param") which Java intercepts silently
            webEngine.setOnAlert(event -> {
                String data = event.getData();
                System.out.println("WebEngine Alert Intercepted: " + data);
                if (data != null) {
                    if (data.startsWith("navigate:")) {
                        String screen = data.substring("navigate:".length());
                        javafx.application.Platform.runLater(() -> {
                            MainController main = MainController.getInstance();
                            if (main != null) {
                                main.navigateTo(screen);
                            } else {
                                System.out.println("MainController instance is null during navigateTo(" + screen + ")");
                            }
                        });
                    } else if (data.startsWith("selectLocation:")) {
                        String payload = data.substring("selectLocation:".length());
                        javafx.application.Platform.runLater(() -> {
                            String[] parts = payload.split("\\|");
                            Session.setSelectedLocation(parts[0]);
                            if (parts.length >= 3) {
                                try {
                                    Session.setUserLat(Double.parseDouble(parts[1]));
                                    Session.setUserLon(Double.parseDouble(parts[2]));
                                } catch (NumberFormatException ignored) {}
                            }
                        });
                    } else if (data.startsWith("openRealMap:")) {
                        String[] parts = data.substring(12).split(",", 3);
                        double lat = Double.parseDouble(parts[0]);
                        double lon = Double.parseDouble(parts[1]);
                        String loc = java.net.URLDecoder.decode(parts[2], StandardCharsets.UTF_8);
                        
                        javafx.application.Platform.runLater(() -> {
                            Session.setSelectedLocation(loc);
                            UiHelper.showInfo("Location marked as: " + loc + "!\nOpening Real Google Maps in your browser.");
                            try {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://www.google.com/maps/search/veterinary+clinic+near+" + lat + "," + lon));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else if (data.equals("getExactLocation")) {
                        new Thread(() -> {
                        String bssids = "";
                        try {
                            // Fetch Wi-Fi BSSIDs for Google Geolocation API
                            Process pWifi = Runtime.getRuntime().exec("netsh wlan show networks mode=bssid");
                            String wifiOut = new String(pWifi.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                            java.util.List<String> macs = new java.util.ArrayList<>();
                            for (String line : wifiOut.split("\n")) {
                                if (line.trim().startsWith("BSSID")) {
                                    String mac = line.substring(line.indexOf(":") + 1).trim();
                                    macs.add(mac);
                                }
                            }
                            bssids = String.join(",", macs);
                        } catch (Exception e) {}
                        
                        final String finalBssids = bssids;

                        try {
                            // Run PowerShell to fetch the exact coordinates from Windows Location Service
                            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command",
                                "Add-Type -AssemblyName System.Device; $w = New-Object System.Device.Location.GeoCoordinateWatcher; $w.Start(); Start-Sleep -Seconds 2; Write-Output \"$($w.Position.Location.Latitude) $($w.Position.Location.Longitude)\"");
                            Process p = pb.start();
                            String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                            if (output.contains(" ")) {
                                String[] parts = output.split(" ");
                                double lat = Double.parseDouble(parts[0]);
                                double lon = Double.parseDouble(parts[1]);
                                if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                                    javafx.application.Platform.runLater(() -> {
                                        webEngine.executeScript("exactLocationCallback(" + lat + ", " + lon + ", '" + finalBssids + "')");
                                    });
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        // If PowerShell fails or location is disabled, fallback with BSSIDs
                        javafx.application.Platform.runLater(() -> {
                            webEngine.executeScript("exactLocationCallback(null, null, '" + finalBssids + "')");
                        });
                    }).start();
                }
                }
            });
            
            updateHomeUI();
        }
    }

    private void updateHomeUI() {
        if (webEngine == null) return;
        
        double userLat = Session.getUserLat();
        double userLon = Session.getUserLon();
        String selectedLocation = Session.getSelectedLocation();
        
        try {
            java.net.URL htmlUrl = getClass().getResource("/html/home.html");
            if (htmlUrl != null) {
                String html = new String(htmlUrl.openStream().readAllBytes(), StandardCharsets.UTF_8);
                
                // Set base URL so relative image paths load from the resources/images directory
                java.net.URL baseUrl = getClass().getResource("/images/");
                if (baseUrl != null) {
                    html = html.replace("<head>", "<head>\n<base href=\"" + baseUrl.toExternalForm() + "\">");
                }
                
                // Replace variables (resilient to spaces added by IDE auto-formatters)
                html = html.replaceAll("\\$\\{\\s*USER_LAT\\s*\\}", String.valueOf(userLat))
                           .replaceAll("\\$\\{\\s*USER_LON\\s*\\}", String.valueOf(userLon))
                           .replaceAll("\\$\\{\\s*LOCATION\\s*\\}", selectedLocation.equals("All") ? "" : selectedLocation)
                           .replaceAll("\\$\\{\\s*IS_MAP_VISIBLE\\s*\\}", !selectedLocation.equals("All") ? "true" : "false");
                
                // Write to a temp file to bypass JavaFX loadContent() UTF-8 bug
                java.io.File tempHtml = java.io.File.createTempFile("pawconnect_home_", ".html");
                tempHtml.deleteOnExit();
                java.nio.file.Files.writeString(tempHtml.toPath(), html, java.nio.charset.StandardCharsets.UTF_8);
                
                webEngine.load(tempHtml.toURI().toString());
            } else {
                System.err.println("Could not find /html/home.html");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateNotifications(JsonNode unread) {
        if (notificationsBox == null) return;
        notificationsBox.getChildren().clear();
        for (JsonNode n : unread) {
            String type = n.path("type").asText();
            String msg = n.path("message").asText();
            Long id = n.path("id").asLong();

            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);");
            
            Label title = new Label(type + " Notification");
            title.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 14px;");
            
            Label body = new Label(msg);
            body.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            body.setWrapText(true);
            
            card.getChildren().addAll(title, body);
            
            // Hover effect
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #34495e; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 5);"));
            card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);"));
            
            // Click to read
            card.setOnMouseClicked(e -> {
                try {
                    ApiClient.get().putJson("/notifications/" + id + "/read", java.util.Map.of());
                } catch (Exception ex) {}
                
                // Navigate
                if ("CHAT".equals(type)) {
                    MainController.getInstance().navigateTo("chat");
                } else {
                    MainController.getInstance().navigateTo("moments");
                }
            });
            
            notificationsBox.getChildren().add(card);
        }
    }
}
