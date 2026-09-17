package com.catconnect.controller;

import com.catconnect.dto.*;
import com.catconnect.entity.*;
import com.catconnect.repository.*;
import com.catconnect.service.AdminGuard;
import com.catconnect.service.CatalogContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final AdminGuard adminGuard;
    private final VetRepository vetRepository;
    private final CatShopRepository catShopRepository;
    private final ShelterRepository shelterRepository;
    private final CatalogContentService catalogContentService;

    private Long requireAdmin(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        adminGuard.assertAdmin(userId);
        return userId;
    }

    // ── Vets ──────────────────────────────────────────────────────

    @GetMapping("/vets")
    public List<Vet> vets(Authentication auth) {
        requireAdmin(auth);
        return vetRepository.findAll();
    }

    @PostMapping("/vets")
    public Vet createVet(@Valid @RequestBody VetRequest request, Authentication auth) {
        Long userId = requireAdmin(auth);
        return vetRepository.save(Vet.builder()
                .userId(userId)
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .rating(request.getRating())
                .emergency(request.getEmergency() != null && request.getEmergency())
                .openHours(request.getOpenHours())
                .city(request.getCity())
                .imageUrl(request.getImageUrl())
                .mapLink(request.getMapLink())
                .build());
    }

    @PutMapping("/vets/{id}")
    public Vet updateVet(@PathVariable Long id, @RequestBody VetUpdateRequest request, Authentication auth) {
        requireAdmin(auth);
        return catalogContentService.adminUpdateVet(id, request);
    }

    @DeleteMapping("/vets/{id}")
    public Map<String, String> deleteVet(@PathVariable Long id, Authentication auth) {
        requireAdmin(auth);
        catalogContentService.adminDeleteVet(id);
        return Map.of("message", "Vet removed");
    }

    // ── Shops ─────────────────────────────────────────────────────

    @GetMapping("/shops")
    public List<CatShop> shops(Authentication auth) {
        requireAdmin(auth);
        return catShopRepository.findAll();
    }

    @PostMapping("/shops")
    public CatShop createShop(@Valid @RequestBody ShopRequest request, Authentication auth) {
        Long userId = requireAdmin(auth);
        return catShopRepository.save(CatShop.builder()
                .userId(userId)
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .rating(request.getRating())
                .city(request.getCity())
                .imageUrl(request.getImageUrl())
                .mapLink(request.getMapLink())
                .build());
    }

    @PutMapping("/shops/{id}")
    public CatShop updateShop(@PathVariable Long id, @RequestBody ShopUpdateRequest request, Authentication auth) {
        requireAdmin(auth);
        return catalogContentService.adminUpdateShop(id, request);
    }

    @DeleteMapping("/shops/{id}")
    public Map<String, String> deleteShop(@PathVariable Long id, Authentication auth) {
        requireAdmin(auth);
        catalogContentService.adminDeleteShop(id);
        return Map.of("message", "Shop removed");
    }

    // ── Shelters ──────────────────────────────────────────────────

    @GetMapping("/shelters")
    public List<Shelter> shelters(Authentication auth) {
        requireAdmin(auth);
        return shelterRepository.findAll();
    }

    @PostMapping("/shelters")
    public Shelter createShelter(@Valid @RequestBody ShelterRequest request, Authentication auth) {
        Long userId = requireAdmin(auth);
        return shelterRepository.save(Shelter.builder()
                .userId(userId)
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .city(request.getCity())
                .imageUrl(request.getImageUrl())
                .mapLink(request.getMapLink())
                .build());
    }

    @PutMapping("/shelters/{id}")
    public Shelter updateShelter(@PathVariable Long id, @RequestBody ShelterUpdateRequest request, Authentication auth) {
        requireAdmin(auth);
        return catalogContentService.adminUpdateShelter(id, request);
    }

    @DeleteMapping("/shelters/{id}")
    public Map<String, String> deleteShelter(@PathVariable Long id, Authentication auth) {
        requireAdmin(auth);
        catalogContentService.adminDeleteShelter(id);
        return Map.of("message", "Shelter removed");
    }
}

