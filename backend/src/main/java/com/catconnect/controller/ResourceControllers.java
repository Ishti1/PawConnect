package com.catconnect.controller;

import com.catconnect.dto.*;
import com.catconnect.entity.*;
import com.catconnect.repository.*;
import com.catconnect.service.CatalogContentService;
import com.catconnect.service.DonationService;
import com.catconnect.service.LostFoundService;
import com.catconnect.service.UserContentService;
import com.catconnect.util.LikeCounter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ResourceControllers {

    private final VetRepository vetRepository;
    private final CatShopRepository catShopRepository;
    private final ShelterRepository shelterRepository;
    private final FoodRecommendationRepository foodRecommendationRepository;
    private final CatMemeRepository catMemeRepository;
    private final AdoptionListingRepository adoptionListingRepository;
    private final LostFoundPostRepository lostFoundPostRepository;
    private final CatMomentRepository catMomentRepository;
    private final DonationCampaignRepository donationCampaignRepository;
    private final DonationService donationService;
    private final LostFoundService lostFoundService;
    private final UserContentService userContentService;
    private final CatalogContentService catalogContentService;

    @GetMapping("/api/vets")
    public List<Vet> vets(@org.springframework.web.bind.annotation.RequestParam(required = false) String location) {
        if (location != null && !location.trim().isEmpty() && !"All".equalsIgnoreCase(location)) {
            return vetRepository.findByCityContainingIgnoreCase(location.trim());
        }
        return vetRepository.findAll();
    }

    @GetMapping("/api/vets/emergency")
    public List<Vet> emergencyVets(@org.springframework.web.bind.annotation.RequestParam(required = false) String location) {
        if (location != null && !location.trim().isEmpty() && !"All".equalsIgnoreCase(location)) {
            return vetRepository.findByEmergencyTrueAndCityContainingIgnoreCase(location.trim());
        }
        return vetRepository.findByEmergencyTrue();
    }

    @PostMapping("/api/vets/{id}/react")
    public Vet reactVet(@PathVariable Long id) {
        Vet vet = vetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vet not found"));
        vet.setLikes(LikeCounter.next(vet.getLikes()));
        return vetRepository.save(vet);
    }

    @GetMapping("/api/shops")
    public List<CatShop> shops() {
        return catShopRepository.findAll();
    }

    @PostMapping("/api/shops/{id}/react")
    public CatShop reactShop(@PathVariable Long id) {
        CatShop shop = catShopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        shop.setLikes(LikeCounter.next(shop.getLikes()));
        return catShopRepository.save(shop);
    }

    @GetMapping("/api/shelters")
    public List<Shelter> shelters() {
        return shelterRepository.findAll();
    }

    @PostMapping("/api/shelters/{id}/react")
    public Shelter reactShelter(@PathVariable Long id) {
        Shelter shelter = shelterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shelter not found"));
        shelter.setLikes(LikeCounter.next(shelter.getLikes()));
        return shelterRepository.save(shelter);
    }

    @GetMapping("/api/food-recommendations")
    public List<FoodRecommendation> food(@RequestParam(required = false) String ageGroup) {
        if (ageGroup != null && !ageGroup.isBlank()) {
            return foodRecommendationRepository.findByAgeGroup(ageGroup);
        }
        return foodRecommendationRepository.findAll();
    }

    @PostMapping("/api/food-recommendations/{id}/react")
    public FoodRecommendation reactFood(@PathVariable Long id) {
        FoodRecommendation food = foodRecommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food item not found"));
        food.setLikes(LikeCounter.next(food.getLikes()));
        return foodRecommendationRepository.save(food);
    }

    @GetMapping("/api/memes")
    public List<CatMeme> memes() {
        return catMemeRepository.findAll();
    }

    @PostMapping("/api/memes")
    public CatMeme createMeme(@Valid @RequestBody MemeRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return catMemeRepository.save(CatMeme.builder()
                .userId(userId)
                .title(request.getTitle())
                .imageUrl(request.getImageUrl())
                .build());
    }

    @PostMapping("/api/memes/{id}/like")
    public CatMeme likeMeme(@PathVariable Long id) {
        CatMeme meme = catMemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meme not found"));
        meme.setLikes(LikeCounter.next(meme.getLikes()));
        return catMemeRepository.save(meme);
    }

    @PostMapping("/api/memes/{id}/react")
    public CatMeme reactMeme(@PathVariable Long id) {
        return likeMeme(id);
    }

    @DeleteMapping("/api/memes/{id}")
    public Map<String, String> deleteMeme(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        userContentService.deleteMeme(id, userId);
        return Map.of("message", "Meme removed");
    }

    @PutMapping("/api/memes/{id}")
    public CatMeme updateMeme(@PathVariable Long id, @RequestBody MemeUpdateRequest request,
                              Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userContentService.updateMeme(id, userId, request);
    }

    @GetMapping("/api/adoptions")
    public List<AdoptionListing> adoptions() {
        return adoptionListingRepository.findByStatus("AVAILABLE");
    }

    @PostMapping("/api/adoptions")
    public AdoptionListing createAdoption(@Valid @RequestBody AdoptionRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return adoptionListingRepository.save(AdoptionListing.builder()
                .userId(userId)
                .catName(request.getCatName())
                .breed(request.getBreed())
                .ageMonths(request.getAgeMonths())
                .gender(request.getGender())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .shelterId(request.getShelterId())
                .build());
    }

    @PostMapping("/api/adoptions/{id}/react")
    public AdoptionListing reactAdoption(@PathVariable Long id) {
        AdoptionListing listing = adoptionListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        listing.setLikes(LikeCounter.next(listing.getLikes()));
        return adoptionListingRepository.save(listing);
    }

    @DeleteMapping("/api/adoptions/{id}")
    public Map<String, String> deleteAdoption(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        userContentService.deleteAdoption(id, userId);
        return Map.of("message", "Listing removed");
    }

    @PutMapping("/api/adoptions/{id}")
    public AdoptionListing updateAdoption(@PathVariable Long id, @RequestBody AdoptionUpdateRequest request,
                                          Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userContentService.updateAdoption(id, userId, request);
    }

    @GetMapping("/api/lost-found")
    public List<LostFoundPost> lostFound() {
        // Now it calls the service method to attach the senderName before returning!
        return lostFoundService.getAllActivePosts();
    }

    @PostMapping("/api/lost-found")
    public LostFoundPost createLostFound(@Valid @RequestBody LostFoundRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return lostFoundPostRepository.save(LostFoundPost.builder()
                .userId(userId)
                .postType(request.getPostType())
                .catDescription(request.getCatDescription())
                .lastSeenLocation(request.getLastSeenLocation())
                .contactPhone(request.getContactPhone())
                .imageUrl(request.getImageUrl())
                .mapLink(request.getMapLink())
                .build());
    }

    @PostMapping("/api/lost-found/{id}/react")
    public LostFoundPost reactLostFound(@PathVariable Long id) {
        LostFoundPost post = lostFoundPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.setLikes(LikeCounter.next(post.getLikes()));
        return lostFoundPostRepository.save(post);
    }

    @DeleteMapping("/api/lost-found/{id}")
    public Map<String, String> deleteLostFound(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        lostFoundService.remove(id, userId);
        return Map.of("message", "Post removed");
    }

    @PutMapping("/api/lost-found/{id}")
    public LostFoundPost updateLostFound(@PathVariable Long id, @RequestBody LostFoundUpdateRequest request,
                                         Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return lostFoundService.update(id, userId, request);
    }

    @GetMapping("/api/moments")
    public List<CatMoment> moments(Authentication auth) {
        if (auth != null) {
            return catMomentRepository.findAllByOrderByCreatedAtDesc();
        }
        return catMomentRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping("/api/moments")
    public CatMoment createMoment(@Valid @RequestBody MomentRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return catMomentRepository.save(CatMoment.builder()
                .userId(userId)
                .caption(request.getCaption())
                .imageUrl(request.getImageUrl())
                .build());
    }

    @PostMapping("/api/moments/{id}/react")
    public CatMoment reactMoment(@PathVariable Long id) {
        CatMoment moment = catMomentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Moment not found"));
        moment.setLikes(LikeCounter.next(moment.getLikes()));
        return catMomentRepository.save(moment);
    }

    @DeleteMapping("/api/moments/{id}")
    public Map<String, String> deleteMoment(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        userContentService.deleteMoment(id, userId);
        return Map.of("message", "Moment removed");
    }

    @PutMapping("/api/moments/{id}")
    public CatMoment updateMoment(@PathVariable Long id, @RequestBody MomentUpdateRequest request,
                                  Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userContentService.updateMoment(id, userId, request);
    }

    @GetMapping("/api/donations")
    public List<DonationCampaign> campaigns() {
        return donationCampaignRepository.findByStatus("ACTIVE");
    }

    @PostMapping("/api/donations")
    public DonationCampaign createCampaign(@Valid @RequestBody CampaignRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return donationCampaignRepository.save(DonationCampaign.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .goalAmount(request.getGoalAmount())
                .shelterId(request.getShelterId())
                .imageUrl(request.getImageUrl())
                .accountName(request.getAccountName())
                .accountNumber(request.getAccountNumber())
                .bankName(request.getBankName())
                .mobileBanking(request.getMobileBanking())
                .paymentInstructions(request.getPaymentInstructions())
                .build());
    }

    @PutMapping("/api/donations/{id}")
    public DonationCampaign updateCampaign(@PathVariable Long id, @RequestBody CampaignUpdateRequest request,
                                           Authentication auth) {
        return catalogContentService.updateCampaign(id, (Long) auth.getPrincipal(), request);
    }

    @DeleteMapping("/api/donations/{id}")
    public Map<String, String> deleteCampaign(@PathVariable Long id, Authentication auth) {
        catalogContentService.deleteCampaign(id, (Long) auth.getPrincipal());
        return Map.of("message", "Campaign removed");
    }

    @PostMapping("/api/donations/{id}/react")
    public DonationCampaign reactCampaign(@PathVariable Long id) {
        DonationCampaign c = donationCampaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        c.setLikes(LikeCounter.next(c.getLikes()));
        return donationCampaignRepository.save(c);
    }

    @PostMapping("/api/donations/{id}/contribute")
    public Donation contribute(@PathVariable Long id, @Valid @RequestBody DonationRequest request,
                               Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return donationService.contribute(id, userId, request);
    }
}
