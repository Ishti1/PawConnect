package com.catconnect.websocket;

import com.catconnect.security.JwtService;
import com.catconnect.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = resolveUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid or missing token"));
            return;
        }
        sessionUsers.put(session.getId(), userId);
        chatService.registerSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = sessionUsers.get(session.getId());
        if (userId == null) {
            return;
        }
        JsonNode node = objectMapper.readTree(message.getPayload());

        if ("subscribe".equals(node.path("type").asText())) {
            String roomId = ChatService.normalizeRoomId(userId, node.path("roomId").asText(ChatService.GENERAL_ROOM));
            chatService.setSessionRoom(session.getId(), roomId);
            return;
        }

        String content = node.path("content").asText("");
        if (content.isBlank()) {
            return;
        }
        String roomId = ChatService.normalizeRoomId(userId, node.path("roomId").asText(ChatService.GENERAL_ROOM));
        chatService.setSessionRoom(session.getId(), roomId);
        chatService.send(userId, content, roomId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionUsers.remove(session.getId());
        chatService.unregisterSession(session);
    }

    private Long resolveUserId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        for (String param : uri.getQuery().split("&")) {
            if (param.startsWith("token=")) {
                String token = param.substring(6);
                if (jwtService.isValid(token)) {
                    return jwtService.extractUserId(token);
                }
            }
        }
        return null;
    }
}
