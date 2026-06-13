package com.catconnect.util;

import com.catconnect.model.User;

public final class Session {

    private static String apiBaseUrl;
    private static String token;
    private static User currentUser;

    private Session() {}

    public static void init() {
        apiBaseUrl = ClientConfig.loadApiUrl();
    }

    public static String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public static String getToken() {
        return token;
    }

    public static void setAuth(String authToken, User user) {
        token = authToken;
        currentUser = user;
    }

    public static void clear() {
        token = null;
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }
}
