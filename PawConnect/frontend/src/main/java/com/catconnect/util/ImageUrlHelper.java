package com.catconnect.util;

/**
 * Turns API-relative image paths (/api/files/...) into full URLs for JavaFX Image.
 */
public final class ImageUrlHelper {

    private ImageUrlHelper() {}

    public static String resolve(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        String apiBase = Session.getApiBaseUrl();
        String origin = apiBase.endsWith("/api")
                ? apiBase.substring(0, apiBase.length() - 4)
                : apiBase.replace("/api", "");
        return origin + (url.startsWith("/") ? url : "/" + url);
    }
}
