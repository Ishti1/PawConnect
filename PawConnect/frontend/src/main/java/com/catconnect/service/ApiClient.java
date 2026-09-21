package com.catconnect.service;

import com.catconnect.model.AuthResponse;
import com.catconnect.util.Session;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static ApiClient get() {
        return INSTANCE;
    }

    public AuthResponse login(String email, String password) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(Map.of("email", email, "password", password));
        return post("/auth/login", body, AuthResponse.class, false);
    }

    public AuthResponse loginWithGoogle(String idToken) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(Map.of("idToken", idToken));
        return post("/auth/google", body, AuthResponse.class, false);
    }

    public void updateDisplayName(String newDisplayName) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(Map.of("displayName", newDisplayName));
        patch("/auth/display-name", body);
    }

    public AuthResponse register(String email, String password, String displayName) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(Map.of(
                "email", email,
                "password", password,
                "displayName", displayName
        ));
        return post("/auth/register", body, AuthResponse.class, false);
    }

    public boolean isBackendReachable() {
        try {
            get("/health");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public JsonNode getList(String path) throws IOException, InterruptedException {
        return get(path);
    }

    public JsonNode postJson(String path, Map<String, Object> body) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(body);
        return post(path, json, JsonNode.class, true);
    }

    public JsonNode putJson(String path, Map<String, Object> body) throws IOException, InterruptedException {
        if (Session.getToken() == null) {
            throw new IOException("You must be logged in.");
        }
        String json = mapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(Session.getApiBaseUrl() + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Session.getToken())
                .PUT(HttpRequest.BodyPublishers.ofString(json));
        HttpResponse<String> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IOException(toUserMessage(e), e);
        }
        if (response.statusCode() >= 400) {
            throw new IOException(parseError(response.body()));
        }
        return mapper.readTree(response.body());
    }

    /**
     * Upload an image file. Returns the URL path returned by the server (e.g. /api/files/moments/uuid.jpg).
     */
    public String uploadImage(File file, String category) throws IOException, InterruptedException {
        if (Session.getToken() == null) {
            throw new IOException("You must be logged in to upload photos.");
        }
        if (file == null || !file.isFile()) {
            throw new IOException("No image file selected.");
        }

        String boundary = "----CatConnect" + System.currentTimeMillis();
        String fileName = file.getName();
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null || !contentType.startsWith("image/")) {
            contentType = "image/jpeg";
        }

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String crlf = "\r\n";
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + boundary + crlf).getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + crlf)
                .getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Type: " + contentType + crlf + crlf).getBytes(StandardCharsets.UTF_8));
        body.write(fileBytes);
        body.write(crlf.getBytes(StandardCharsets.UTF_8));
        body.write(("--" + boundary + "--" + crlf).getBytes(StandardCharsets.UTF_8));

        String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);
        URI uri = URI.create(Session.getApiBaseUrl() + "/upload?category=" + encodedCategory);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IOException(toUserMessage(e), e);
        }
        if (response.statusCode() >= 400) {
            throw new IOException(parseError(response.body()));
        }
        JsonNode node = mapper.readTree(response.body());
        if (!node.has("url")) {
            throw new IOException("Upload failed: no URL in response.");
        }
        return node.get("url").asText();
    }

    /** Download image bytes over HTTP and set on the ImageView (JavaFX thread). */
    public void loadImageAsync(String imageUrl, ImageView target) {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(imageUrl))
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();
                HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() >= 400) {
                    return;
                }
                byte[] bytes = response.body();
                Platform.runLater(() -> target.setImage(new Image(new ByteArrayInputStream(bytes))));
            } catch (Exception ignored) {
                // Leave placeholder empty on failure
            }
        }).start();
    }

    public void delete(String path) throws IOException, InterruptedException {
        if (Session.getToken() == null) {
            throw new IOException("You must be logged in to delete posts.");
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(Session.getApiBaseUrl() + path))
                .timeout(Duration.ofSeconds(30))
                .DELETE();
        builder.header("Authorization", "Bearer " + Session.getToken());
        HttpResponse<String> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IOException(toUserMessage(e), e);
        }
        if (response.statusCode() >= 400) {
            throw new IOException(parseError(response.body()));
        }
    }

    private <T> T post(String path, String body, Class<T> type, boolean auth) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(Session.getApiBaseUrl() + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (auth) {
            if (Session.getToken() == null) {
                throw new IOException("You must be logged in.");
            }
            builder.header("Authorization", "Bearer " + Session.getToken());
        }
        HttpResponse<String> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IOException(toUserMessage(e), e);
        }
        if (response.statusCode() >= 400) {
            throw new IOException(parseError(response.body()));
        }
        try {
            return mapper.readValue(response.body(), type);
        } catch (Exception e) {
            throw new IOException("Invalid server response. Is the backend running on port 8080?", e);
        }
    }

    private void patch(String path, String body) throws IOException, InterruptedException {
        if (Session.getToken() == null) {
            throw new IOException("You must be logged in.");
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(Session.getApiBaseUrl() + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Session.getToken())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body));
        HttpResponse<String> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IOException(toUserMessage(e), e);
        }
        if (response.statusCode() >= 400) {
            throw new IOException(parseError(response.body()));
        }
    }

    private JsonNode get(String path) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(Session.getApiBaseUrl() + path))
                .timeout(Duration.ofSeconds(30))
                .GET();
        if (Session.getToken() != null) {
            builder.header("Authorization", "Bearer " + Session.getToken());
        }
        HttpResponse<String> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IOException(toUserMessage(e), e);
        }
        if (response.statusCode() >= 400) {
            throw new IOException(parseError(response.body()));
        }
        return mapper.readTree(response.body());
    }

    private String toUserMessage(Exception e) {
        Throwable c = e;
        while (c != null) {
            if (c instanceof ConnectException) {
                return "Cannot connect to backend at " + Session.getApiBaseUrl()
                        + ". Start CatConnectApplication in Eclipse first (port 8080).";
            }
            c = c.getCause();
        }
        return e.getMessage() != null ? e.getMessage() : "Network error";
    }

    private String parseError(String body) {
        try {
            JsonNode node = mapper.readTree(body);
            if (node.has("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {}
        return "Request failed";
    }
}
