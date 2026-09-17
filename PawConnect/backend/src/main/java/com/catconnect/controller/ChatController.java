package com.catconnect.controller;

import com.catconnect.dto.ChatMessageDto;
import com.catconnect.dto.ChatMessageRequest;
import com.catconnect.dto.UserDto;
import com.catconnect.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/messages")
    public List<ChatMessageDto> getMessages(
            @RequestParam(required = false) String roomId,
            Authentication auth) {
        String room = roomId != null ? roomId : ChatService.GENERAL_ROOM;
        return chatService.getHistory(room);
    }

    @GetMapping("/contacts")
    public List<UserDto> getContacts(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.getUniqueChatPartners(userId);
    }

    @PostMapping("/messages")
    public ChatMessageDto sendMessage(@RequestBody ChatMessageRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String roomId = request.getRoomId() != null ? request.getRoomId() : ChatService.GENERAL_ROOM;
        return chatService.send(userId, request.getContent(), roomId);
    }
    
    @PostMapping("/messages/{id}/react")
    public ChatMessageDto reactToMessage(@PathVariable Long id, @RequestBody Map<String, String> payload, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.reactToMessage(id, payload.get("reaction"), userId);
    }
}
