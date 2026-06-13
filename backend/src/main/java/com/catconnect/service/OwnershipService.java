package com.catconnect.service;

import org.springframework.stereotype.Service;

@Service
public class OwnershipService {

    public void assertOwner(Long contentOwnerId, Long currentUserId) {
        if (contentOwnerId == null || currentUserId == null || !contentOwnerId.equals(currentUserId)) {
            throw new IllegalArgumentException("You can only delete your own posts");
        }
    }
}
