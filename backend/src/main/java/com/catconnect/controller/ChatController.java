package com.catconnect.controller;

import com.catconnect.dto.ChatMessageDto;
import com.catconnect.dto.ChatSendRequest;
import com.catconnect.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/messages")
    public List<ChatMessageDto> messages(@RequestParam(required = false) String roomId) {
        return chatService.recentMessages(roomId);
    }

    @PostMapping("/messages")
    public ChatMessageDto send(@Valid @RequestBody ChatSendRequest request, Authentication auth) throws Exception {
        Long userId = (Long) auth.getPrincipal();
        return chatService.send(userId, request.getContent(), request.getRoomId());
    }
}
