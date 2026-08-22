package com.catconnect.service;

import com.catconnect.dto.*;
import com.catconnect.entity.*;
import com.catconnect.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogContentService {

    private final VetRepository vetRepository;
    private final CatShopRepository catShopRepository;
    private final ShelterRepository shelterRepository;
    private final CareKnowledgeRepository careKnowledgeRepository;
    private final DonationCampaignRepository donationCampaignRepository;
    private final OwnershipService ownershipService;

    // ── Admin versions (no ownership checks) ───────────────────────

    @Transactional
    public Vet adminUpdateVet(Long id, VetUpdateRequest req) {
        Vet vet = vetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vet not found"));
        applyVet(vet, req);
        return vetRepository.save(vet);
    }

    @Transactional
    public void adminDeleteVet(Long id) {
        Vet vet = vetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vet not found"));
        vetRepository.delete(vet);
    }

    @Transactional
    public CatShop adminUpdateShop(Long id, ShopUpdateRequest req) {
        CatShop shop = catShopRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        if (req.getName() != null && !req.getName().isBlank()) shop.setName(req.getName());
        if (req.getAddress() != null) shop.setAddress(req.getAddress());
        if (req.getPhone() != null) shop.setPhone(req.getPhone());
        if (req.getRating() != null) shop.setRating(req.getRating());
        if (req.getCity() != null) shop.setCity(req.getCity());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) shop.setImageUrl(req.getImageUrl());
        if (req.getMapLink() != null) shop.setMapLink(req.getMapLink());
        return catShopRepository.save(shop);
    }

    @Transactional
    public void adminDeleteShop(Long id) {
        CatShop shop = catShopRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        catShopRepository.delete(shop);
    }

    @Transactional
    public Shelter adminUpdateShelter(Long id, ShelterUpdateRequest req) {
        Shelter s = shelterRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shelter not found"));
        if (req.getName() != null && !req.getName().isBlank()) s.setName(req.getName());
        if (req.getAddress() != null) s.setAddress(req.getAddress());
        if (req.getPhone() != null) s.setPhone(req.getPhone());
        if (req.getEmail() != null) s.setEmail(req.getEmail());
        if (req.getWebsite() != null) s.setWebsite(req.getWebsite());
        if (req.getDescription() != null) s.setDescription(req.getDescription());
        if (req.getCapacity() != null) s.setCapacity(req.getCapacity());
        if (req.getCity() != null) s.setCity(req.getCity());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) s.setImageUrl(req.getImageUrl());
        if (req.getMapLink() != null) s.setMapLink(req.getMapLink());
        return shelterRepository.save(s);
    }

    @Transactional
    public void adminDeleteShelter(Long id) {
        Shelter s = shelterRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shelter not found"));
        shelterRepository.delete(s);
    }


    @Transactional
    public Vet updateVet(Long id, Long userId, VetUpdateRequest req) {
        Vet vet = vetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vet not found"));
        ownershipService.assertOwner(vet.getUserId(), userId);
        applyVet(vet, req);
        return vetRepository.save(vet);
    }

    @Transactional
    public void deleteVet(Long id, Long userId) {
        Vet vet = vetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vet not found"));
        ownershipService.assertOwner(vet.getUserId(), userId);
        vetRepository.delete(vet);
    }

    @Transactional
    public CatShop updateShop(Long id, Long userId, ShopUpdateRequest req) {
        CatShop shop = catShopRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        ownershipService.assertOwner(shop.getUserId(), userId);
        if (req.getName() != null && !req.getName().isBlank()) shop.setName(req.getName());
        if (req.getAddress() != null) shop.setAddress(req.getAddress());
        if (req.getPhone() != null) shop.setPhone(req.getPhone());
        if (req.getRating() != null) shop.setRating(req.getRating());
        if (req.getCity() != null) shop.setCity(req.getCity());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) shop.setImageUrl(req.getImageUrl());
        if (req.getMapLink() != null) shop.setMapLink(req.getMapLink());
        return catShopRepository.save(shop);
    }

    @Transactional
    public void deleteShop(Long id, Long userId) {
        CatShop shop = catShopRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        ownershipService.assertOwner(shop.getUserId(), userId);
        catShopRepository.delete(shop);
    }

    @Transactional
    public Shelter updateShelter(Long id, Long userId, ShelterUpdateRequest req) {
        Shelter s = shelterRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shelter not found"));
        ownershipService.assertOwner(s.getUserId(), userId);
        if (req.getName() != null && !req.getName().isBlank()) s.setName(req.getName());
        if (req.getAddress() != null) s.setAddress(req.getAddress());
        if (req.getPhone() != null) s.setPhone(req.getPhone());
        if (req.getEmail() != null) s.setEmail(req.getEmail());
        if (req.getWebsite() != null) s.setWebsite(req.getWebsite());
        if (req.getDescription() != null) s.setDescription(req.getDescription());
        if (req.getCapacity() != null) s.setCapacity(req.getCapacity());
        if (req.getCity() != null) s.setCity(req.getCity());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) s.setImageUrl(req.getImageUrl());
        if (req.getMapLink() != null) s.setMapLink(req.getMapLink());
        return shelterRepository.save(s);
    }

    @Transactional
    public void deleteShelter(Long id, Long userId) {
        Shelter s = shelterRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shelter not found"));
        ownershipService.assertOwner(s.getUserId(), userId);
        shelterRepository.delete(s);
    }

    @Transactional
    public CareKnowledge updateKnowledge(Long id, Long userId, KnowledgeUpdateRequest req) {
        CareKnowledge k = careKnowledgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        ownershipService.assertOwner(k.getUserId(), userId);
        if (req.getTitle() != null && !req.getTitle().isBlank()) k.setTitle(req.getTitle());
        if (req.getCategory() != null && !req.getCategory().isBlank()) k.setCategory(req.getCategory());
        if (req.getContent() != null && !req.getContent().isBlank()) k.setContent(req.getContent());
        if (req.getAuthor() != null) k.setAuthor(req.getAuthor());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) k.setImageUrl(req.getImageUrl());
        return careKnowledgeRepository.save(k);
    }

    @Transactional
    public void deleteKnowledge(Long id, Long userId) {
        CareKnowledge k = careKnowledgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        ownershipService.assertOwner(k.getUserId(), userId);
        careKnowledgeRepository.delete(k);
    }


    @Transactional
    public DonationCampaign updateCampaign(Long id, Long userId, CampaignUpdateRequest req) {
        DonationCampaign c = donationCampaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        ownershipService.assertOwner(c.getUserId(), userId);
        if (req.getTitle() != null && !req.getTitle().isBlank()) c.setTitle(req.getTitle());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getGoalAmount() != null) c.setGoalAmount(req.getGoalAmount());
        if (req.getShelterId() != null) c.setShelterId(req.getShelterId());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) c.setImageUrl(req.getImageUrl());
        if (req.getAccountName() != null) c.setAccountName(req.getAccountName());
        if (req.getAccountNumber() != null) c.setAccountNumber(req.getAccountNumber());
        if (req.getBankName() != null) c.setBankName(req.getBankName());
        if (req.getMobileBanking() != null) c.setMobileBanking(req.getMobileBanking());
        if (req.getPaymentInstructions() != null) c.setPaymentInstructions(req.getPaymentInstructions());
        return donationCampaignRepository.save(c);
    }

    @Transactional
    public void deleteCampaign(Long id, Long userId) {
        DonationCampaign c = donationCampaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        ownershipService.assertOwner(c.getUserId(), userId);
        c.setStatus("REMOVED");
        donationCampaignRepository.save(c);
    }

    private void applyVet(Vet vet, VetUpdateRequest req) {
        if (req.getName() != null && !req.getName().isBlank()) vet.setName(req.getName());
        if (req.getAddress() != null) vet.setAddress(req.getAddress());
        if (req.getPhone() != null) vet.setPhone(req.getPhone());
        if (req.getRating() != null) vet.setRating(req.getRating());
        if (req.getEmergency() != null) vet.setEmergency(req.getEmergency());
        if (req.getOpenHours() != null) vet.setOpenHours(req.getOpenHours());
        if (req.getCity() != null) vet.setCity(req.getCity());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) vet.setImageUrl(req.getImageUrl());
        if (req.getMapLink() != null) vet.setMapLink(req.getMapLink());
    }
}
