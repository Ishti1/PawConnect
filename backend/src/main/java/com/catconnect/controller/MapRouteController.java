package com.catconnect.controller;

import com.catconnect.service.RouteService;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapRouteController {

    private final RouteService routeService;


    /*
     * =========================================================
     * ROUTE ENDPOINT
     * =========================================================
     *
     * Example:
     *
     * /api/map/route
     * ?fromLat=23.7465
     * &fromLon=90.3760
     * &toLat=23.7500
     * &toLon=90.3900
     */

    @GetMapping("/route")
    public ResponseEntity<?> getRoute(

            @RequestParam double fromLat,

            @RequestParam double fromLon,

            @RequestParam double toLat,

            @RequestParam double toLon
    ) {

        try {

            RouteService.RouteResult route =
                    routeService.getDrivingRoute(
                            fromLat,
                            fromLon,
                            toLat,
                            toLon
                    );


            Map<String, Object> response =
                    new LinkedHashMap<>();


            response.put(
                    "success",
                    true
            );


            response.put(
                    "distanceMeters",
                    route.distanceMeters()
            );


            response.put(
                    "distanceKm",
                    route.distanceKm()
            );


            response.put(
                    "durationSeconds",
                    route.durationSeconds()
            );


            response.put(
                    "durationMinutes",
                    route.durationMinutes()
            );


            /*
             * GeoJSON LineString returned by OSRM.
             */
            response.put(
                    "geometry",
                    route.geometry()
            );


            return ResponseEntity.ok(
                    response
            );


        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            errorResponse(
                                    e.getMessage()
                            )
                    );


        } catch (Exception e) {

            e.printStackTrace();


            return ResponseEntity
                    .internalServerError()
                    .body(
                            errorResponse(
                                    e.getMessage() == null
                                            ? "Could not calculate route."
                                            : e.getMessage()
                            )
                    );
        }
    }


    /*
     * =========================================================
     * ERROR RESPONSE
     * =========================================================
     */

    private Map<String, Object> errorResponse(
            String message
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();


        response.put(
                "success",
                false
        );


        response.put(
                "message",
                message
        );


        return response;
    }
}