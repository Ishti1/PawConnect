package com.catconnect.config;

import com.catconnect.entity.CatShop;
import com.catconnect.entity.Vet;
import com.catconnect.repository.CatShopRepository;
import com.catconnect.repository.VetRepository;
import com.catconnect.service.GeocodingService;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Order(100)
public class MapDataRepairRunner implements CommandLineRunner {

    private final VetRepository vetRepository;
    private final CatShopRepository catShopRepository;
    private final GeocodingService geocodingService;

    @Override
    public void run(String... args) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("PawConnect map coordinate repair started");
        System.out.println("========================================");

        repairVets();
        repairShops();

        System.out.println("========================================");
        System.out.println("PawConnect map coordinate repair finished");
        System.out.println("========================================");
        System.out.println();
    }

    /*
     * =====================================================
     * VETS
     * =====================================================
     */

    private void repairVets() {

        for (Vet vet : vetRepository.findAll()) {

            /*
             * Skip records that already contain usable
             * coordinates.
             */
            if (hasValidCoordinates(
                    vet.getLatitude(),
                    vet.getLongitude()
            )) {

                System.out.println(
                        "✓ Vet already has coordinates: "
                                + vet.getName()
                                + " -> "
                                + vet.getLatitude()
                                + ", "
                                + vet.getLongitude()
                );

                continue;
            }

            System.out.println();
            System.out.println(
                    "Geocoding vet: "
                            + vet.getName()
            );

            System.out.println(
                    "Address: "
                            + vet.getAddress()
            );

            System.out.println(
                    "City: "
                            + vet.getCity()
            );

            geocodingService
                    .geocodeBusiness(
                            vet.getName(),
                            vet.getAddress(),
                            vet.getCity()
                    )
                    .ifPresentOrElse(

                            coordinates -> {

                                /*
                                 * Safety check:
                                 * PawConnect currently expects these
                                 * locations to be in/around Dhaka.
                                 */
                                if (!coordinateLooksLikeDhaka(
                                        coordinates.latitude(),
                                        coordinates.longitude()
                                )) {

                                    System.err.println(
                                            "✗ Rejected suspicious coordinates for vet: "
                                                    + vet.getName()
                                                    + " -> "
                                                    + coordinates.latitude()
                                                    + ", "
                                                    + coordinates.longitude()
                                    );

                                    return;
                                }

                                vet.setLatitude(
                                        coordinates.latitude()
                                );

                                vet.setLongitude(
                                        coordinates.longitude()
                                );

                                vetRepository.save(vet);

                                System.out.println(
                                        "✓ Vet geocoded successfully: "
                                                + vet.getName()
                                                + " -> "
                                                + coordinates.latitude()
                                                + ", "
                                                + coordinates.longitude()
                                );
                            },

                            () -> {

                                System.err.println(
                                        "✗ Could not geocode vet: "
                                                + vet.getName()
                                );
                            }
                    );
        }
    }

    /*
     * =====================================================
     * SHOPS
     * =====================================================
     */

    private void repairShops() {

        for (CatShop shop : catShopRepository.findAll()) {

            /*
             * Skip records that already contain usable
             * coordinates.
             */
            if (hasValidCoordinates(
                    shop.getLatitude(),
                    shop.getLongitude()
            )) {

                System.out.println(
                        "✓ Shop already has coordinates: "
                                + shop.getName()
                                + " -> "
                                + shop.getLatitude()
                                + ", "
                                + shop.getLongitude()
                );

                continue;
            }

            System.out.println();
            System.out.println(
                    "Geocoding shop: "
                            + shop.getName()
            );

            System.out.println(
                    "Address: "
                            + shop.getAddress()
            );

            System.out.println(
                    "City: "
                            + shop.getCity()
            );

            geocodingService
                    .geocodeBusiness(
                            shop.getName(),
                            shop.getAddress(),
                            shop.getCity()
                    )
                    .ifPresentOrElse(

                            coordinates -> {

                                if (!coordinateLooksLikeDhaka(
                                        coordinates.latitude(),
                                        coordinates.longitude()
                                )) {

                                    System.err.println(
                                            "✗ Rejected suspicious coordinates for shop: "
                                                    + shop.getName()
                                                    + " -> "
                                                    + coordinates.latitude()
                                                    + ", "
                                                    + coordinates.longitude()
                                    );

                                    return;
                                }

                                shop.setLatitude(
                                        coordinates.latitude()
                                );

                                shop.setLongitude(
                                        coordinates.longitude()
                                );

                                catShopRepository.save(shop);

                                System.out.println(
                                        "✓ Shop geocoded successfully: "
                                                + shop.getName()
                                                + " -> "
                                                + coordinates.latitude()
                                                + ", "
                                                + coordinates.longitude()
                                );
                            },

                            () -> {

                                System.err.println(
                                        "✗ Could not geocode shop: "
                                                + shop.getName()
                                );
                            }
                    );
        }
    }

    /*
     * =====================================================
     * Coordinate validation
     * =====================================================
     */

    private boolean hasValidCoordinates(
            BigDecimal latitude,
            BigDecimal longitude
    ) {

        if (latitude == null
                || longitude == null) {

            return false;
        }

        /*
         * 0,0 is not a valid PawConnect business location.
         */
        return latitude.compareTo(BigDecimal.ZERO) != 0
                &&
                longitude.compareTo(BigDecimal.ZERO) != 0;
    }

    /*
     * =====================================================
     * Dhaka-area sanity check
     * =====================================================
     */

    private boolean coordinateLooksLikeDhaka(
            BigDecimal latitude,
            BigDecimal longitude
    ) {

        if (latitude == null
                || longitude == null) {

            return false;
        }

        double lat =
                latitude.doubleValue();

        double lon =
                longitude.doubleValue();

        /*
         * Deliberately generous bounding box around Dhaka.
         *
         * This prevents a bad geocoder result from putting
         * a vet/shop in another country.
         */
        return lat >= 23.60
                && lat <= 24.00
                && lon >= 90.20
                && lon <= 90.60;
    }
}