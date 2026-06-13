package com.catconnect.service;

import com.catconnect.entity.AdoptionListing;
import com.catconnect.entity.CatMeme;
import com.catconnect.entity.CatMoment;
import com.catconnect.dto.AdoptionUpdateRequest;
import com.catconnect.dto.MemeUpdateRequest;
import com.catconnect.dto.MomentUpdateRequest;
import com.catconnect.repository.AdoptionListingRepository;
import com.catconnect.repository.CatMemeRepository;
import com.catconnect.repository.CatMomentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserContentService {

    private final CatMomentRepository catMomentRepository;
    private final CatMemeRepository catMemeRepository;
    private final AdoptionListingRepository adoptionListingRepository;
    private final OwnershipService ownershipService;

    @Transactional
    public void deleteMoment(Long id, Long currentUserId) {
        CatMoment moment = catMomentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Moment not found"));
        ownershipService.assertOwner(moment.getUserId(), currentUserId);
        catMomentRepository.delete(moment);
    }

    @Transactional
    public CatMoment updateMoment(Long id, Long currentUserId, MomentUpdateRequest req) {
        CatMoment moment = catMomentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Moment not found"));
        ownershipService.assertOwner(moment.getUserId(), currentUserId);
        if (req.getCaption() != null) moment.setCaption(req.getCaption());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) moment.setImageUrl(req.getImageUrl());
        return catMomentRepository.save(moment);
    }

    @Transactional
    public void deleteMeme(Long id, Long currentUserId) {
        CatMeme meme = catMemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meme not found"));
        ownershipService.assertOwner(meme.getUserId(), currentUserId);
        catMemeRepository.delete(meme);
    }

    @Transactional
    public CatMeme updateMeme(Long id, Long currentUserId, MemeUpdateRequest req) {
        CatMeme meme = catMemeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meme not found"));
        ownershipService.assertOwner(meme.getUserId(), currentUserId);
        if (req.getTitle() != null) meme.setTitle(req.getTitle());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) meme.setImageUrl(req.getImageUrl());
        return catMemeRepository.save(meme);
    }

    @Transactional
    public void deleteAdoption(Long id, Long currentUserId) {
        AdoptionListing listing = adoptionListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        ownershipService.assertOwner(listing.getUserId(), currentUserId);
        listing.setStatus("REMOVED");
        adoptionListingRepository.save(listing);
    }

    @Transactional
    public AdoptionListing updateAdoption(Long id, Long currentUserId, AdoptionUpdateRequest req) {
        AdoptionListing listing = adoptionListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        ownershipService.assertOwner(listing.getUserId(), currentUserId);
        if (req.getCatName() != null && !req.getCatName().isBlank()) listing.setCatName(req.getCatName());
        if (req.getBreed() != null) listing.setBreed(req.getBreed());
        if (req.getAgeMonths() != null) listing.setAgeMonths(req.getAgeMonths());
        if (req.getGender() != null) listing.setGender(req.getGender());
        if (req.getDescription() != null) listing.setDescription(req.getDescription());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) listing.setImageUrl(req.getImageUrl());
        return adoptionListingRepository.save(listing);
    }
}
