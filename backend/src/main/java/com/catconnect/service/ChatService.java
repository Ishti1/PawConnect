package com.catconnect.service;

import com.catconnect.dto.ChatMessageDto;
import com.catconnect.entity.ChatMessage;
import com.catconnect.entity.User;
import com.catconnect.repository.ChatMessageRepository;
import com.catconnect.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {

    public static final String GENERAL_ROOM = "general";

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void registerSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void unregisterSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public List<ChatMessageDto> recentMessages(String roomId) {
        String room = roomId != null && !roomId.isBlank() ? roomId : GENERAL_ROOM;
        List<ChatMessage> messages = chatMessageRepository.findTop100ByRoomIdOrderBySentAtDesc(room);
        return messages.stream()
                .map(ChatMessageDto::from)
                .sorted((a, b) -> a.getSentAt().compareTo(b.getSentAt()))
                .toList();
    }

    @Transactional
    public ChatMessageDto send(Long userId, String content, String roomId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String room = roomId != null && !roomId.isBlank() ? roomId : GENERAL_ROOM;
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .senderId(userId)
                .senderName(user.getDisplayName())
                .content(content.trim())
                .roomId(room)
                .build());
        ChatMessageDto dto = ChatMessageDto.from(saved);
        broadcast(dto);
        return dto;
    }

    public void broadcast(ChatMessageDto dto) throws Exception {
        String json = objectMapper.writeValueAsString(dto);
        TextMessage message = new TextMessage(json);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(message);
            }
        }
    }

    public Set<WebSocketSession> getSessions() {
        return Collections.unmodifiableSet(sessions);
    }
}
