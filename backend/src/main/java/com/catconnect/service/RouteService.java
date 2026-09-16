package com.catconnect.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RouteService {

    private static final String OSRM_BASE_URL =
            "https://router.project-osrm.org";

    private final RestClient restClient;


    public RouteService() {

        this.restClient =
                RestClient.builder()
                        .baseUrl(OSRM_BASE_URL)
                        .defaultHeader(
                                "User-Agent",
                                "PawConnect/1.0 University Project"
                        )
                        .build();
    }


    /*
     * =========================================================
     * GET DRIVING ROUTE
     * =========================================================
     *
     * IMPORTANT:
     *
     * OSRM expects:
     *
     * longitude,latitude
     *
     * NOT:
     *
     * latitude,longitude
     */

    public RouteResult getDrivingRoute(
            double fromLat,
            double fromLon,
            double toLat,
            double toLon
    ) {

        validateCoordinates(
                fromLat,
                fromLon
        );

        validateCoordinates(
                toLat,
                toLon
        );


        String path =
                "/route/v1/driving/"
                        + fromLon
                        + ","
                        + fromLat
                        + ";"
                        + toLon
                        + ","
                        + toLat
                        + "?overview=full"
                        + "&geometries=geojson"
                        + "&steps=false"
                        + "&alternatives=false";


        JsonNode response =
                restClient
                        .get()
                        .uri(path)
                        .retrieve()
                        .body(
                                JsonNode.class
                        );


        if (response == null) {

            throw new IllegalStateException(
                    "OSRM returned an empty response."
            );
        }


        String code =
                response
                        .path("code")
                        .asText("");


        if (!"Ok".equalsIgnoreCase(code)) {

            throw new IllegalStateException(
                    "OSRM could not calculate a route. Code: "
                            + code
            );
        }


        JsonNode routes =
                response.path(
                        "routes"
                );


        if (!routes.isArray()
                || routes.isEmpty()) {

            throw new IllegalStateException(
                    "No route was found."
            );
        }


        JsonNode route =
                routes.get(0);


        double distanceMeters =
                route
                        .path("distance")
                        .asDouble();


        double durationSeconds =
                route
                        .path("duration")
                        .asDouble();


        JsonNode geometry =
                route.path(
                        "geometry"
                );


        if (geometry.isMissingNode()
                || geometry.isNull()) {

            throw new IllegalStateException(
                    "Route geometry was not returned."
            );
        }


        return new RouteResult(
                distanceMeters,
                durationSeconds,
                geometry
        );
    }


    /*
     * =========================================================
     * VALIDATION
     * =========================================================
     */

    private void validateCoordinates(
            double latitude,
            double longitude
    ) {

        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)) {

            throw new IllegalArgumentException(
                    "Coordinates must be valid numbers."
            );
        }


        if (latitude < -90
                || latitude > 90) {

            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90."
            );
        }


        if (longitude < -180
                || longitude > 180) {

            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180."
            );
        }
    }


    /*
     * =========================================================
     * RESULT
     * =========================================================
     */

    public record RouteResult(
            double distanceMeters,
            double durationSeconds,
            JsonNode geometry
    ) {

        public double distanceKm() {

            return distanceMeters / 1000.0;
        }


        public double durationMinutes() {

            return durationSeconds / 60.0;
        }
    }
}