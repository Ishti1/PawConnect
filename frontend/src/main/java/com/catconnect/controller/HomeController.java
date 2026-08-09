package com.catconnect.controller;

import com.catconnect.util.Session;
import com.catconnect.util.UiHelper;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.nio.charset.StandardCharsets;

public class HomeController {

    @FXML
    private WebView homeWebView;

    private WebEngine webEngine;
    
    @FXML
    public void initialize() {
        if (homeWebView != null) {
            webEngine = homeWebView.getEngine();
            webEngine.setJavaScriptEnabled(true);
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 PawConnect/1.0");
            
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
                        String loc = data.substring("selectLocation:".length());
                        javafx.application.Platform.runLater(() -> {
                            Session.setSelectedLocation(loc);
                            UiHelper.showInfo("Location saved to " + loc + "! Showing nearby services.");
                        });
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
}
