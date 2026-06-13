package com.catconnect.service;

import com.catconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminGuard {

    private final UserRepository userRepository;

    public void assertAdmin(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getIsAdmin() == null || !user.getIsAdmin()) {
            throw new IllegalArgumentException("Admin access required");
        }
    }
}

