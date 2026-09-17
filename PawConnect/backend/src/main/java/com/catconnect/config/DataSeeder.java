package com.catconnect.config;

import com.catconnect.entity.*;
import com.catconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VetRepository vetRepository;
    private final CatShopRepository catShopRepository;
    private final ShelterRepository shelterRepository;
    private final CareKnowledgeRepository careKnowledgeRepository;
    private final CatMemeRepository catMemeRepository;
    private final AdoptionListingRepository adoptionListingRepository;
    private final LostFoundPostRepository lostFoundPostRepository;
    private final CatMomentRepository catMomentRepository;
    private final DonationCampaignRepository donationCampaignRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEMO_EMAIL = "demo@catconnect.com";
    private static final String DEMO_PASSWORD = "password123";
    private static final boolean DEMO_IS_ADMIN = true;


    @Override
    public void run(String... args) {

        ensureDemoUser();

        if (vetRepository.count() == 0) {

            seedSampleData();
        }
    }

    /** Always ensure demo login works (fixes bad password from SQL seed). */
    private void ensureDemoUser() {
        userRepository.findByEmail(DEMO_EMAIL).ifPresentOrElse(
                user -> {
                    user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
                    user.setIsAdmin(DEMO_IS_ADMIN);
                    userRepository.save(user);
                },
                () -> userRepository.save(User.builder()
                        .email(DEMO_EMAIL)
                        .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                        .displayName("Demo User")
                        .city("Cairo")
                        .isAdmin(DEMO_IS_ADMIN)
                        .build())
        );
    }

    private void seedSampleData() {
        User demo = userRepository.findByEmail(DEMO_EMAIL).orElseThrow();

        vetRepository.saveAll(java.util.List.of(

                Vet.builder()
                        .name("Mayer Doa Veterinary")
                        .address("Road-3, Sector-5, Uttara, Dhaka-1230")
                        .phone("017171717171")
                        .latitude(new BigDecimal("23.875854"))
                        .longitude(new BigDecimal("90.379543"))
                        .rating(new BigDecimal("4.8"))
                        .emergency(false)
                        .openHours("9AM-8PM Daily")
                        .city("Dhaka")
                        .userId(demo.getId())
                        .imageUrl("https://placecats.com/320/200")
                        .build(),

                Vet.builder()
                        .name("Pet Hospital 24/7")
                        .address("Dhanmondi 27, Dhaka")
                        .phone("012345678")
                        .latitude(new BigDecimal("23.746466"))
                        .longitude(new BigDecimal("90.376015"))
                        .rating(new BigDecimal("4.9"))
                        .emergency(true)
                        .openHours("24 Hours")
                        .city("Dhaka")
                        .userId(demo.getId())
                        .imageUrl("https://placecats.com/321/200")
                        .build(),

                Vet.builder()
                        .name("Fermillion Veterinary Clinic")
                        .address("Tajmahal Road, Mohammadpur, Dhaka")
                        .phone("01600011644")
                        .latitude(new BigDecimal("23.766742"))
                        .longitude(new BigDecimal("90.358741"))
                        .rating(new BigDecimal("4.5"))
                        .emergency(false)
                        .openHours("10AM-6PM Sun-Thu")
                        .city("Dhaka")
                        .userId(demo.getId())
                        .imageUrl("https://placecats.com/322/200")
                        .build()
        ));

        catShopRepository.saveAll(java.util.List.of(

                CatShop.builder()
                        .name("Meow Mart")
                        .address("Dhanmondi, Dhaka")
                        .phone("01700000001")
                        .latitude(new BigDecimal("23.746998"))
                        .longitude(new BigDecimal("90.374482"))
                        .rating(new BigDecimal("4.6"))
                        .city("Dhaka")
                        .userId(demo.getId())
                        .imageUrl("https://placecats.com/323/200")
                        .build(),

                CatShop.builder()
                        .name("Kitty Kingdom Supplies")
                        .address("Banani, Dhaka")
                        .phone("01700000002")
                        .latitude(new BigDecimal("23.793743"))
                        .longitude(new BigDecimal("90.406600"))
                        .rating(new BigDecimal("4.4"))
                        .city("Dhaka")
                        .userId(demo.getId())
                        .imageUrl("https://placecats.com/324/200")
                        .build()
        ));

        Shelter shelter = shelterRepository.save(Shelter.builder()
                .name("Cairo Cat Rescue").address("30 Desert Rd").phone("+20-2-8888-0001")
                .email("info@cairocats.org").description("Non-profit rescue for stray cats.")
                .capacity(120).city("Cairo").userId(demo.getId())
                .imageUrl("https://placecats.com/325/200").build());

        careKnowledgeRepository.save(CareKnowledge.builder()
                .title("Kitten First Week Checklist").category("Kittens")
                .content("Schedule vet visit within 48 hours. Provide warm bedding and kitten formula if under 4 weeks.")
                .author("Dr. Amira Hassan").userId(demo.getId())
                .imageUrl("https://placecats.com/326/200").build());

        catMemeRepository.save(CatMeme.builder()
                .userId(demo.getId())
                .title("Monday Mood").imageUrl("https://placecats.com/400/300").likes(1240).build());

        adoptionListingRepository.save(AdoptionListing.builder()
                .userId(demo.getId())
                .catName("Luna").breed("Domestic Shorthair").ageMonths(8).gender("Female")
                .description("Playful tabby, vaccinated.").imageUrl("https://placecats.com/200/200")
                .shelterId(shelter.getId()).build());

        lostFoundPostRepository.save(LostFoundPost.builder()
                .userId(demo.getId()).postType(LostFoundPost.PostType.LOST)
                .catDescription("Orange tabby, white paws, green collar Max")
                .lastSeenLocation("Zamalek, 15th St").contactPhone("+20-100-111-2222")
                .imageUrl("https://placecats.com/300/250").build());

        catMomentRepository.save(CatMoment.builder()
                .userId(demo.getId()).caption("Sunbeam nap champion")
                .imageUrl("https://placecats.com/150/150")
                .mediaType("IMAGE").likes(24).build());

        donationCampaignRepository.save(DonationCampaign.builder()
                .title("Winter Shelter Blanket Drive")
                .description("Help us buy warm blankets for rescued cats.")
                .shelterId(shelter.getId()).goalAmount(new BigDecimal("15000"))
                .raisedAmount(new BigDecimal("8750"))
                .userId(demo.getId())
                .imageUrl("https://placecats.com/350/200").build());
    }
    private void ensureMapCoordinates() {

        catShopRepository.findAll().forEach(shop -> {

            String name =
                    shop.getName() == null
                            ? ""
                            : shop.getName().trim().toLowerCase();

            if (name.contains("uttara pet mart")) {

                shop.setLatitude(
                        new BigDecimal("23.8759")
                );

                shop.setLongitude(
                        new BigDecimal("90.3795")
                );

                shop.setCity("Uttara");

                catShopRepository.save(shop);
            }

            else if (name.contains("pet essentials")) {

                shop.setLatitude(
                        new BigDecimal("23.8717")
                );

                shop.setLongitude(
                        new BigDecimal("90.3853")
                );

                shop.setCity("Uttara");

                catShopRepository.save(shop);
            }

            else if (name.contains("pet zone gulshan")) {

                shop.setLatitude(
                        new BigDecimal("23.7925")
                );

                shop.setLongitude(
                        new BigDecimal("90.4078")
                );

                shop.setCity("Gulshan");

                catShopRepository.save(shop);
            }
        });


        vetRepository.findAll().forEach(vet -> {

            String name =
                    vet.getName() == null
                            ? ""
                            : vet.getName().trim().toLowerCase();

            /*
             * Add your actual vet names here
             * in exactly the same way.
             */

        });


        System.out.println(
                "PawConnect map coordinates updated."
        );
    }
}
