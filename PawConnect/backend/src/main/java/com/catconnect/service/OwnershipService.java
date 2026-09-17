package com.catconnect.service;

import com.catconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final UserRepository userRepository;

    public void assertOwner(Long contentOwnerId, Long currentUserId) {
        // Admin users bypass ownership checks — they can manage any content
        if (isAdmin(currentUserId)) return;
        if (contentOwnerId == null || currentUserId == null || !contentOwnerId.equals(currentUserId)) {
            throw new IllegalArgumentException("You can only delete your own posts");
        }
    }

    public boolean isAdmin(Long userId) {
        if (userId == null) return false;
        return userRepository.findById(userId)
                .map(u -> Boolean.TRUE.equals(u.getIsAdmin()))
                .orElse(false);
    }
}
