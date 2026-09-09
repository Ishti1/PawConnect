package com.catconnect.controller;

import com.catconnect.dto.*;
import com.catconnect.entity.*;
import com.catconnect.repository.*;
import com.catconnect.service.CatalogContentService;
import com.catconnect.service.DonationService;
import com.catconnect.service.LostFoundService;
import com.catconnect.service.NotificationService;
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
    private final CatMemeRepository catMemeRepository;
    private final AdoptionListingRepository adoptionListingRepository;
    private final LostFoundPostRepository lostFoundPostRepository;
    private final CatMomentRepository catMomentRepository;
    private final DonationCampaignRepository donationCampaignRepository;
    private final DonationService donationService;
    private final LostFoundService lostFoundService;
    private final UserContentService userContentService;
    private final CatalogContentService catalogContentService;
    private final com.catconnect.repository.UserRepository userRepository;
    private final NotificationService notificationService;

    private String getDisplayName(Long userId) {
        return userRepository.findById(userId)
                .map(com.catconnect.entity.User::getDisplayName)
                .orElse("Anonymous");
    }

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
    public Vet reactVet(@PathVariable Long id, Authentication auth) {
        Vet vet = vetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vet not found"));
        Long userId = (Long) auth.getPrincipal();
        if (vet.getLikedBy().contains(userId)) {
            vet.getLikedBy().remove(userId);
            vet.setLikes(Math.max(0, vet.getLikes() - 1));
        } else {
            vet.getLikedBy().add(userId);
            vet.setLikes(vet.getLikes() + 1);
        }
        return vetRepository.save(vet);
    }

    @GetMapping("/api/shops")
    public List<CatShop> shops() {
        return catShopRepository.findAll();
    }

    @PostMapping("/api/shops/{id}/react")
    public CatShop reactShop(@PathVariable Long id, Authentication auth) {
        CatShop shop = catShopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        Long userId = (Long) auth.getPrincipal();
        if (shop.getLikedBy().contains(userId)) {
            shop.getLikedBy().remove(userId);
            shop.setLikes(Math.max(0, shop.getLikes() - 1));
        } else {
            shop.getLikedBy().add(userId);
            shop.setLikes(shop.getLikes() + 1);
        }
        return catShopRepository.save(shop);
    }

    @GetMapping("/api/shelters")
    public List<Shelter> shelters() {
        return shelterRepository.findAll();
    }

    @PostMapping("/api/shelters/{id}/react")
    public Shelter reactShelter(@PathVariable Long id, Authentication auth) {
        Shelter shelter = shelterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shelter not found"));
        Long userId = (Long) auth.getPrincipal();
        if (shelter.getLikedBy().contains(userId)) {
            shelter.getLikedBy().remove(userId);
            shelter.setLikes(Math.max(0, shelter.getLikes() - 1));
        } else {
            shelter.getLikedBy().add(userId);
            shelter.setLikes(shelter.getLikes() + 1);
        }
        return shelterRepository.save(shelter);
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
    public CatMeme likeMeme(@PathVariable Long id, Authentication auth) {
        CatMeme meme = catMemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meme not found"));
        Long userId = (Long) auth.getPrincipal();
        if (meme.getLikedBy().contains(userId)) {
            meme.getLikedBy().remove(userId);
            meme.setLikes(Math.max(0, meme.getLikes() - 1));
        } else {
            meme.getLikedBy().add(userId);
            meme.setLikes(meme.getLikes() + 1);
        }
        return catMemeRepository.save(meme);
    }

    @PostMapping("/api/memes/{id}/react")
    public CatMeme reactMeme(@PathVariable Long id, Authentication auth) {
        return likeMeme(id, auth);
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
        return userContentService.getAllActiveAdoptions();
    }

    @PostMapping("/api/adoptions")
    public AdoptionListing createAdoption(@Valid @RequestBody AdoptionRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        AdoptionListing listing = adoptionListingRepository.save(AdoptionListing.builder()
                .userId(userId)
                .catName(request.getCatName())
                .breed(request.getBreed())
                .ageMonths(request.getAgeMonths())
                .gender(request.getGender())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .shelterId(request.getShelterId())
                .location(request.getLocation())
                .address(request.getAddress())
                .contactPhone(request.getContactPhone())
                .build());
        listing.setSenderName(getDisplayName(userId));
        return listing;
    }

    @PostMapping("/api/adoptions/{id}/react")
    public AdoptionListing reactAdoption(@PathVariable Long id, Authentication auth) {
        AdoptionListing listing = adoptionListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        Long userId = (Long) auth.getPrincipal();
        if (listing.getLikedBy().contains(userId)) {
            listing.getLikedBy().remove(userId);
            listing.setLikes(Math.max(0, listing.getLikes() - 1));
        } else {
            listing.getLikedBy().add(userId);
            listing.setLikes(listing.getLikes() + 1);
        }
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
        AdoptionListing listing = userContentService.updateAdoption(id, userId, request);
        listing.setSenderName(getDisplayName(userId));
        return listing;
    }

    @GetMapping("/api/lost-found")
    public List<LostFoundPost> lostFound() {
        // Now it calls the service method to attach the senderName before returning!
        return lostFoundService.getAllActivePosts();
    }

    @PostMapping("/api/lost-found")
    public LostFoundPost createLostFound(@Valid @RequestBody LostFoundRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        LostFoundPost post = lostFoundPostRepository.save(LostFoundPost.builder()
                .userId(userId)
                .postType(request.getPostType())
                .catDescription(request.getCatDescription())
                .lastSeenLocation(request.getLastSeenLocation())
                .contactPhone(request.getContactPhone())
                .imageUrl(request.getImageUrl())
                .mapLink(request.getMapLink())
                .build());
        post.setSenderName(getDisplayName(userId));
        return post;
    }

    @PostMapping("/api/lost-found/{id}/react")
    public LostFoundPost reactLostFound(@PathVariable Long id, Authentication auth) {
        LostFoundPost post = lostFoundPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        Long userId = (Long) auth.getPrincipal();
        if (post.getLikedBy().contains(userId)) {
            post.getLikedBy().remove(userId);
            post.setLikes(Math.max(0, post.getLikes() - 1));
        } else {
            post.getLikedBy().add(userId);
            post.setLikes(post.getLikes() + 1);
        }
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
        LostFoundPost post = lostFoundService.update(id, userId, request);
        post.setSenderName(getDisplayName(userId));
        return post;
    }

    @GetMapping("/api/moments")
    public List<MomentResponse> moments(Authentication auth) {
        return userContentService.getAllMoments();
    }

    @PostMapping("/api/moments")
    public CatMoment createMoment(@Valid @RequestBody MomentRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return catMomentRepository.save(CatMoment.builder()
                .userId(userId)
                .caption(request.getCaption())
                .imageUrl(request.getImageUrl())
                .mediaType(request.getMediaType())
                .sharedMomentId(request.getSharedMomentId())
                .build());
    }

    @PostMapping("/api/moments/{id}/comments")
    public CatMomentComment addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userContentService.addComment(id, userId, req);
    }

    @GetMapping("/api/moments/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable Long id) {
        return userContentService.getComments(id);
    }

    @PostMapping("/api/moments/{id}/share")
    public CatMoment shareMoment(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userContentService.shareMoment(id, userId);
    }

    @PostMapping("/api/moments/{id}/react")
    public CatMoment reactMoment(@PathVariable Long id, Authentication auth) {
        CatMoment moment = catMomentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Moment not found"));
        
        Long userId = (Long) auth.getPrincipal();
        if (moment.getLikedBy().contains(userId)) {
            moment.getLikedBy().remove(userId);
            moment.setLikes(Math.max(0, moment.getLikes() - 1));
        } else {
            moment.getLikedBy().add(userId);
            moment.setLikes(moment.getLikes() + 1);
            if (!moment.getUserId().equals(userId)) {
                String reactorName = getDisplayName(userId);
                notificationService.createNotification(moment.getUserId(), reactorName + " reacted to your post", "REACTION", userId);
            }
        }
        
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
        return catalogContentService.getAllActiveCampaigns();
    }

    @PostMapping("/api/donations")
    public DonationCampaign createCampaign(@Valid @RequestBody CampaignRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        DonationCampaign c = donationCampaignRepository.save(DonationCampaign.builder()
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
                .contactPhone(request.getContactPhone())
                .build());
        c.setSenderName(getDisplayName(userId));
        return c;
    }

    @PutMapping("/api/donations/{id}")
    public DonationCampaign updateCampaign(@PathVariable Long id, @RequestBody CampaignUpdateRequest request,
                                           Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        DonationCampaign c = catalogContentService.updateCampaign(id, userId, request);
        c.setSenderName(getDisplayName(userId));
        return c;
    }

    @DeleteMapping("/api/donations/{id}")
    public Map<String, String> deleteCampaign(@PathVariable Long id, Authentication auth) {
        catalogContentService.deleteCampaign(id, (Long) auth.getPrincipal());
        return Map.of("message", "Campaign removed");
    }

    @PostMapping("/api/donations/{id}/react")
    public DonationCampaign reactCampaign(@PathVariable Long id, Authentication auth) {
        DonationCampaign c = donationCampaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        Long userId = (Long) auth.getPrincipal();
        if (c.getLikedBy().contains(userId)) {
            c.getLikedBy().remove(userId);
            c.setLikes(Math.max(0, c.getLikes() - 1));
        } else {
            c.getLikedBy().add(userId);
            c.setLikes(c.getLikes() + 1);
        }
        return donationCampaignRepository.save(c);
    }

    @PostMapping("/api/donations/{id}/contribute")
    public Donation contribute(@PathVariable Long id, @Valid @RequestBody DonationRequest request,
                               Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return donationService.contribute(id, userId, request);
    }
}
