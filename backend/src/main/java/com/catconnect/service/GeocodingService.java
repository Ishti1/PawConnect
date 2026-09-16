package com.catconnect.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GeocodingService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org";

    private static final String USER_AGENT =
            "PawConnect-University-Project/1.0";

    private final RestClient client;

    public GeocodingService(RestClient.Builder builder) {

        this.client =
                builder
                        .baseUrl(NOMINATIM_URL)
                        .defaultHeader(
                                HttpHeaders.USER_AGENT,
                                USER_AGENT
                        )
                        .build();
    }


    public Optional<Coordinates> geocodeBusiness(
            String name,
            String address,
            String city
    ) {

        List<String> queries =
                buildFallbackQueries(
                        name,
                        address,
                        city
                );


        for (String query : queries) {

            System.out.println(
                    "Trying geocode query: "
                            + query
            );


            Optional<Coordinates> result =
                    geocodeSingle(query);


            if (result.isPresent()) {

                System.out.println(
                        "✓ Query worked: "
                                + query
                );

                return result;
            }


            sleep();
        }


        return Optional.empty();
    }


    private List<String> buildFallbackQueries(
            String name,
            String address,
            String city
    ) {

        List<String> queries =
                new ArrayList<>();


        /*
         * 1. Exact full address.
         *
         * Business names often make Nominatim fail,
         * so address-only is tried first.
         */

        if (address != null
                && !address.isBlank()) {

            queries.add(
                    address.trim()
                            + ", Bangladesh"
            );
        }


        /*
         * 2. Address + city
         */

        if (address != null
                && !address.isBlank()
                && city != null
                && !city.isBlank()) {

            queries.add(
                    address.trim()
                            + ", "
                            + city.trim()
                            + ", Dhaka, Bangladesh"
            );
        }


        /*
         * 3. Simplified address.
         *
         * Removes characters that sometimes confuse
         * geocoding searches.
         */

        if (address != null
                && !address.isBlank()) {

            String simplified =
                    simplifyAddress(address);

            queries.add(
                    simplified
                            + ", Dhaka, Bangladesh"
            );
        }


        /*
         * 4. Business + area.
         */

        if (name != null
                && !name.isBlank()
                && city != null
                && !city.isBlank()) {

            queries.add(
                    name.trim()
                            + ", "
                            + city.trim()
                            + ", Dhaka, Bangladesh"
            );
        }


        /*
         * 5. City / neighborhood fallback.
         *
         * Less precise, but still keeps the marker
         * in the correct PawConnect area.
         */

        if (city != null
                && !city.isBlank()) {

            queries.add(
                    city.trim()
                            + ", Dhaka, Bangladesh"
            );
        }


        return queries;
    }


    private String simplifyAddress(
            String address
    ) {

        return address
                .replace("#", "")
                .replace("/", " ")
                .replace(",", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }


    private Optional<Coordinates> geocodeSingle(
            String query
    ) {

        try {

            JsonNode response =
                    client.get()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path("/search")
                                            .queryParam(
                                                    "q",
                                                    query
                                            )
                                            .queryParam(
                                                    "format",
                                                    "jsonv2"
                                            )
                                            .queryParam(
                                                    "limit",
                                                    1
                                            )
                                            .queryParam(
                                                    "countrycodes",
                                                    "bd"
                                            )
                                            .build()
                            )
                            .retrieve()
                            .body(JsonNode.class);


            if (response == null
                    || !response.isArray()
                    || response.isEmpty()) {

                return Optional.empty();
            }


            JsonNode result =
                    response.get(0);


            String lat =
                    result.path("lat").asText();

            String lon =
                    result.path("lon").asText();


            if (lat.isBlank()
                    || lon.isBlank()) {

                return Optional.empty();
            }


            return Optional.of(

                    new Coordinates(

                            new BigDecimal(lat),

                            new BigDecimal(lon)
                    )
            );

        }

        catch (Exception e) {

            System.err.println(
                    "Geocode request failed: "
                            + query
            );

            return Optional.empty();
        }
    }


    private void sleep() {

        try {

            Thread.sleep(1100);

        }

        catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }
    }


    public record Coordinates(
            BigDecimal latitude,
            BigDecimal longitude
    ) {}
}