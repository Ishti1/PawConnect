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
        if (vetRepository.count() > 0) {
            return;
        }
        seedSampleData();
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
                Vet.builder().name("Mayer Doa Veterinary").address("Road-3,Sector-5, Uttara,Dhaka-1230")
                        .phone("017171717171").rating(new BigDecimal("4.8")).emergency(false)
                        .openHours("9AM-8PM Daily").city("Dhaka").userId(demo.getId())
                        .imageUrl("https://placecats.com/320/200").build(),
                Vet.builder().name("Pet Hospital 24/7").address("Dhanmondi 27")
                        .phone("012345678").rating(new BigDecimal("4.9")).emergency(true)
                        .openHours("24 Hours").city("Dhaka").userId(demo.getId())
                        .imageUrl("https://placecats.com/321/200").build(),
                Vet.builder().name("Fermillion veterinary Clinic").address("Tajmahal Road,Dhaka-1207")
                        .phone("01600011644").rating(new BigDecimal("4.5")).emergency(false)
                        .openHours("10AM-6PM Sun-Thu").city("Dhaka").userId(demo.getId())
                        .imageUrl("https://placecats.com/322/200").build()
        ));

        catShopRepository.saveAll(java.util.List.of(
                CatShop.builder().name("Meow Mart").address("22 Abbas El Akkad St")
                        .phone("+20-2-1111-2222").rating(new BigDecimal("4.6")).city("Cairo").userId(demo.getId())
                        .imageUrl("https://placecats.com/323/200").build(),
                CatShop.builder().name("Kitty Kingdom Supplies").address("5 Mall of Egypt")
                        .phone("+20-2-3333-4444").rating(new BigDecimal("4.4")).city("Giza").userId(demo.getId())
                        .imageUrl("https://placecats.com/324/200").build()
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
}
