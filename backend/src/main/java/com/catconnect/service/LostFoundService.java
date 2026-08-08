package com.catconnect.service;

import com.catconnect.entity.LostFoundPost;
import com.catconnect.dto.LostFoundUpdateRequest;
import com.catconnect.repository.LostFoundPostRepository;
import com.catconnect.repository.UserRepository; // Make sure this is correct for your project
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LostFoundService {

    private final LostFoundPostRepository lostFoundPostRepository;
    private final OwnershipService ownershipService;
    private final UserRepository userRepository;

    // New method to fetch posts and populate the senderName

    @Transactional(readOnly = true)
    public List<LostFoundPost> getAllActivePosts() {
        List<LostFoundPost> posts = lostFoundPostRepository.findByStatus("ACTIVE");

        for (LostFoundPost post : posts) {
            if (post.getUserId() != null) {
                userRepository.findById(post.getUserId()).ifPresent(user -> {
                    // Use getDisplayName() here!
                    post.setSenderName(user.getDisplayName());

                    // Optional: Keep this to verify it works in your console
                    System.out.println("✅ Found user: " + user.getDisplayName() + " for Post ID: " + post.getId());
                });
            }
        }
        return posts;
    }

    @Transactional
    public void remove(Long id, Long currentUserId) {
        LostFoundPost post = lostFoundPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        ownershipService.assertOwner(post.getUserId(), currentUserId);
        post.setStatus("REMOVED");
        lostFoundPostRepository.save(post);
    }

    @Transactional
    public LostFoundPost update(Long id, Long currentUserId, LostFoundUpdateRequest req) {
        LostFoundPost post = lostFoundPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        ownershipService.assertOwner(post.getUserId(), currentUserId);
        if (req.getPostType() != null) post.setPostType(req.getPostType());
        if (req.getCatDescription() != null && !req.getCatDescription().isBlank()) post.setCatDescription(req.getCatDescription());
        if (req.getLastSeenLocation() != null) post.setLastSeenLocation(req.getLastSeenLocation());
        if (req.getContactPhone() != null) post.setContactPhone(req.getContactPhone());
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) post.setImageUrl(req.getImageUrl());
        if (req.getMapLink() != null) post.setMapLink(req.getMapLink());
        return lostFoundPostRepository.save(post);
    }
}