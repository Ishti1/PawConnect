package com.catconnect.util;

import com.catconnect.model.User;

public final class Session {

    private static String apiBaseUrl;
    private static String token;
    private static User currentUser;

    /*
     * Shared location state.
     *
     * Home mini-map and Paw Map both read/write
     * these same values.
     */
    private static String selectedLocation = "All";
    private static double userLat = 0;
    private static double userLon = 0;


    private Session() {
    }


    /*
     * =====================================================
     * APP CONFIG
     * =====================================================
     */

    public static void init() {

        apiBaseUrl =
                ClientConfig.loadApiUrl();
    }


    public static String getApiBaseUrl() {

        return apiBaseUrl;
    }


    /*
     * =====================================================
     * AUTHENTICATION
     * =====================================================
     */

    public static String getToken() {

        return token;
    }


    public static void setAuth(
            String authToken,
            User user
    ) {

        token =
                authToken;

        currentUser =
                user;
    }


    public static User getCurrentUser() {

        return currentUser;
    }


    public static boolean isLoggedIn() {

        return token != null
                && !token.isBlank();
    }


    /*
     * =====================================================
     * LOCATION
     * =====================================================
     */

    public static String getSelectedLocation() {

        return selectedLocation;
    }


    public static void setSelectedLocation(
            String loc
    ) {

        if (loc == null
                || loc.isBlank()) {

            selectedLocation =
                    "All";

        } else {

            selectedLocation =
                    loc;
        }
    }


    public static double getUserLat() {

        return userLat;
    }


    public static void setUserLat(
            double lat
    ) {

        /*
         * Only accept valid latitude values.
         */
        if (Double.isFinite(lat)
                && lat >= -90
                && lat <= 90) {

            userLat =
                    lat;
        }
    }


    public static double getUserLon() {

        return userLon;
    }


    public static void setUserLon(
            double lon
    ) {

        /*
         * Only accept valid longitude values.
         */
        if (Double.isFinite(lon)
                && lon >= -180
                && lon <= 180) {

            userLon =
                    lon;
        }
    }


    /*
     * Returns true only when PawConnect currently
     * has a real user-selected/detected location.
     *
     * 0,0 is treated as "no location".
     */
    public static boolean hasUserLocation() {

        return Double.isFinite(userLat)
                &&
                Double.isFinite(userLon)
                &&
                !(userLat == 0
                        && userLon == 0);
    }


    /*
     * Convenient way to update the complete location
     * from Home or Paw Map.
     */
    public static void setUserLocation(
            double latitude,
            double longitude,
            String locationName
    ) {

        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)) {

            return;
        }


        if (latitude < -90
                || latitude > 90
                || longitude < -180
                || longitude > 180) {

            return;
        }


        if (latitude == 0
                && longitude == 0) {

            return;
        }


        userLat =
                latitude;

        userLon =
                longitude;


        if (locationName == null
                || locationName.isBlank()) {

            selectedLocation =
                    "Selected Location";

        } else {

            selectedLocation =
                    locationName;
        }
    }


    /*
     * Removes only location information.
     */
    public static void clearUserLocation() {

        selectedLocation =
                "All";

        userLat =
                0;

        userLon =
                0;
    }


    /*
     * =====================================================
     * CLEAR SESSION
     * =====================================================
     */

    public static void clear() {

        token =
                null;

        currentUser =
                null;


        /*
         * Important:
         * Do not leave the previous user's location
         * active after logout.
         */
        clearUserLocation();
    }
}