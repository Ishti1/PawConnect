package com.catconnect.service;

import com.catconnect.dto.ChatMessageDto;
import com.catconnect.dto.UserDto;
import com.catconnect.entity.ChatMessage;
import com.catconnect.repository.ChatMessageRepository;
import com.catconnect.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public static final String GENERAL_ROOM = "general";
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRooms = new ConcurrentHashMap<>();

    public static String dmRoomId(Long userA, Long userB) {
        long lo = Math.min(userA, userB);
        long hi = Math.max(userA, userB);
        return "dm_" + lo + "_" + hi;
    }

    public static String normalizeRoomId(Long senderId, String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return GENERAL_ROOM;
        }
        if (roomId.matches("dm_\\d+")) {
            long targetId = Long.parseLong(roomId.substring(3));
            return dmRoomId(senderId, targetId);
        }
        return roomId;
    }

    public void registerSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        sessionRooms.put(session.getId(), GENERAL_ROOM);
    }

    public void unregisterSession(WebSocketSession session) {
        sessions.remove(session.getId());
        sessionRooms.remove(session.getId());
    }

    public void setSessionRoom(String sessionId, String roomId) {
        sessionRooms.put(sessionId, roomId != null ? roomId : GENERAL_ROOM);
    }

    public List<ChatMessageDto> getHistory(String roomId) {
        List<ChatMessage> messages = chatMessageRepository
                .findTop100ByRoomIdInOrderBySentAtDesc(historyRoomIds(roomId));
        Collections.reverse(messages);
        return messages.stream().map(ChatMessageDto::from).toList();
    }

    public ChatMessageDto send(Long senderId, String content, String roomId) {
        String normalizedRoom = normalizeRoomId(senderId, roomId);

        ChatMessage msg = ChatMessage.builder()
                .senderId(senderId)
                .senderName(userRepository.findById(senderId).map(u -> u.getDisplayName()).orElse("User"))
                .content(content)
                .roomId(normalizedRoom)
                .build();

        ChatMessage savedMsg = chatMessageRepository.save(msg);
        ChatMessageDto dto = ChatMessageDto.from(savedMsg);

        try {
            String payload = objectMapper.writeValueAsString(dto);
            for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                WebSocketSession s = entry.getValue();
                String subscribedRoom = sessionRooms.getOrDefault(entry.getKey(), GENERAL_ROOM);
                if (s.isOpen() && subscribedRoom.equals(savedMsg.getRoomId())) {
                    s.sendMessage(new TextMessage(payload));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public ChatMessageDto reactToMessage(Long messageId, String reaction, Long userId) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        msg.setReaction(reaction);
        ChatMessage savedMsg = chatMessageRepository.save(msg);
        ChatMessageDto dto = ChatMessageDto.from(savedMsg);

        // Broadcast the reaction update
        try {
            // We use the same message payload, but the frontend will see the reaction field
            String payload = objectMapper.writeValueAsString(dto);
            for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                WebSocketSession s = entry.getValue();
                String subscribedRoom = sessionRooms.getOrDefault(entry.getKey(), GENERAL_ROOM);
                if (s.isOpen() && subscribedRoom.equals(savedMsg.getRoomId())) {
                    s.sendMessage(new TextMessage(payload));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public List<UserDto> getUniqueChatPartners(Long userId) {
        Set<Long> partnerIds = new LinkedHashSet<>();
        for (String roomId : chatMessageRepository.findDmRoomIdsForUser(userId)) {
            Long partner = partnerFromDmRoom(roomId, userId);
            if (partner != null) {
                partnerIds.add(partner);
            }
        }
        return userRepository.findAllById(partnerIds).stream()
                .map(u -> new UserDto(u.getId(), null, u.getDisplayName(), null, null, null))
                .toList();
    }

    private static List<String> historyRoomIds(String roomId) {
        if (roomId.startsWith("dm_")) {
            String[] parts = roomId.split("_");
            if (parts.length == 3) {
                long lo = Long.parseLong(parts[1]);
                long hi = Long.parseLong(parts[2]);
                return List.of(roomId, "dm_" + lo, "dm_" + hi);
            }
        }
        return List.of(roomId);
    }

    private static Long partnerFromDmRoom(String roomId, Long userId) {
        if (!roomId.startsWith("dm_")) {
            return null;
        }
        String[] parts = roomId.split("_");
        if (parts.length == 3) {
            long lo = Long.parseLong(parts[1]);
            long hi = Long.parseLong(parts[2]);
            if (lo == userId) {
                return hi;
            }
            if (hi == userId) {
                return lo;
            }
            return null;
        }
        if (parts.length == 2) {
            long id = Long.parseLong(parts[1]);
            return id != userId ? id : null;
        }
        return null;
    }
}
