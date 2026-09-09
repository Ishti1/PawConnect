package com.catconnect.controller;

import com.catconnect.dto.NotificationDto;
import com.catconnect.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread")
    public List<NotificationDto> getUnreadNotifications(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return notificationService.getUnreadNotifications(userId);
    }

    @PutMapping("/{id}/read")
    public Map<String, String> markAsRead(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        notificationService.markAsRead(id, userId);
        return Map.of("message", "Marked as read");
    }

    @PutMapping("/read-all")
    public Map<String, String> markAllAsRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        notificationService.markAllAsRead(userId);
        return Map.of("message", "All marked as read");
    }
}
