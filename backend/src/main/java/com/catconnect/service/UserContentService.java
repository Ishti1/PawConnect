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
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserContentService {

    private final CatMomentRepository catMomentRepository;
    private final CatMemeRepository catMemeRepository;
    private final AdoptionListingRepository adoptionListingRepository;
    private final OwnershipService ownershipService;
    private final com.catconnect.repository.UserRepository userRepository;
    private final com.catconnect.repository.CatMomentCommentRepository catMomentCommentRepository;

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

    public List<com.catconnect.dto.MomentResponse> getAllMoments() {
        return catMomentRepository.findAllByOrderByCreatedAtDesc().stream().map(this::mapToMomentResponse).toList();
    }

    private com.catconnect.dto.MomentResponse mapToMomentResponse(CatMoment moment) {
        com.catconnect.entity.User user = userRepository.findById(moment.getUserId()).orElse(null);
        com.catconnect.dto.MomentResponse.MomentResponseBuilder builder = com.catconnect.dto.MomentResponse.builder()
                .id(moment.getId())
                .userId(moment.getUserId())
                .username(user != null ? user.getDisplayName() : "Unknown User")
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .caption(moment.getCaption())
                .imageUrl(moment.getImageUrl())
                .mediaType(moment.getMediaType())
                .likes(moment.getLikes())
                .createdAt(moment.getCreatedAt());

        if (moment.getSharedMomentId() != null) {
            catMomentRepository.findById(moment.getSharedMomentId()).ifPresent(shared -> {
                com.catconnect.entity.User sharedUser = userRepository.findById(shared.getUserId()).orElse(null);
                builder.sharedMoment(com.catconnect.dto.MomentResponse.builder()
                        .id(shared.getId())
                        .userId(shared.getUserId())
                        .username(sharedUser != null ? sharedUser.getDisplayName() : "Unknown User")
                        .avatarUrl(sharedUser != null ? sharedUser.getAvatarUrl() : null)
                        .caption(shared.getCaption())
                        .imageUrl(shared.getImageUrl())
                        .mediaType(shared.getMediaType())
                        .likes(shared.getLikes())
                        .createdAt(shared.getCreatedAt())
                        .build());
            });
        }
        return builder.build();
    }

    @Transactional
    public com.catconnect.entity.CatMomentComment addComment(Long momentId, Long userId, com.catconnect.dto.CommentRequest req) {
        return catMomentCommentRepository.save(com.catconnect.entity.CatMomentComment.builder()
                .momentId(momentId)
                .userId(userId)
                .content(req.getContent())
                .build());
    }

    public List<com.catconnect.dto.CommentResponse> getComments(Long momentId) {
        return catMomentCommentRepository.findByMomentIdOrderByCreatedAtAsc(momentId).stream().map(c -> {
            com.catconnect.entity.User user = userRepository.findById(c.getUserId()).orElse(null);
            return com.catconnect.dto.CommentResponse.builder()
                    .id(c.getId())
                    .userId(c.getUserId())
                    .username(user != null ? user.getDisplayName() : "Unknown User")
                    .avatarUrl(user != null ? user.getAvatarUrl() : null)
                    .content(c.getContent())
                    .createdAt(c.getCreatedAt())
                    .build();
        }).toList();
    }

    @Transactional
    public CatMoment shareMoment(Long originalMomentId, Long currentUserId) {
        CatMoment original = catMomentRepository.findById(originalMomentId)
                .orElseThrow(() -> new IllegalArgumentException("Original moment not found"));
        
        return catMomentRepository.save(CatMoment.builder()
                .userId(currentUserId)
                .caption("Reposted a moment")
                .imageUrl(original.getImageUrl())
                .mediaType(original.getMediaType())
                .sharedMomentId(original.getId())
                .build());
    }
}
