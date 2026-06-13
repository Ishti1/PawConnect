package com.catconnect.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ClientConfig {

    private static final Path CONFIG_PATH =
            Path.of(System.getProperty("user.home"), ".catconnect", "config.properties");
    private static final String KEY_API_URL = "api.url";
    public static final String DEFAULT_API_URL = "http://localhost:8080/api";

    private ClientConfig() {}

    public static String loadApiUrl() {
        String env = System.getenv("CATCONNECT_API_URL");
        if (env != null && !env.isBlank()) {
            return normalizeApiUrl(env);
        }
        Properties properties = load();
        return normalizeApiUrl(properties.getProperty(KEY_API_URL, DEFAULT_API_URL));
    }

    public static void saveApiUrl(String apiUrl) throws IOException {
        Properties properties = load();
        properties.setProperty(KEY_API_URL, normalizeApiUrl(apiUrl));
        Files.createDirectories(CONFIG_PATH.getParent());
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(out, "CatConnect client settings");
        }
    }

    public static String normalizeApiUrl(String apiUrl) {
        String trimmed = apiUrl == null ? "" : apiUrl.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_API_URL;
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (!trimmed.endsWith("/api")) {
            trimmed = trimmed + "/api";
        }
        return trimmed;
    }

    private static Properties load() {
        Properties properties = new Properties();
        if (!Files.isRegularFile(CONFIG_PATH)) {
            return properties;
        }
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            properties.load(in);
        } catch (IOException ignored) {
            // Fall back to defaults.
        }
        return properties;
    }
}
