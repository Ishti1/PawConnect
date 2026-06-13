package com.catconnect.service;

import com.catconnect.util.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class ChatClient {

    private final ObjectMapper mapper = new ObjectMapper();
    private WebSocketClient client;
    private Consumer<JsonNode> onMessage;

    public void connect(Consumer<JsonNode> messageHandler) throws Exception {
        disconnect();
        onMessage = messageHandler;
        String url = toWebSocketUrl(Session.getApiBaseUrl()) + "/ws/chat?token=" + Session.getToken();
        client = new WebSocketClient(URI.create(url)) {
            @Override
            public void onOpen(ServerHandshake handshake) {}

            @Override
            public void onMessage(String message) {
                try {
                    JsonNode node = mapper.readTree(message);
                    if (onMessage != null) {
                        javafx.application.Platform.runLater(() -> onMessage.accept(node));
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {}

            @Override
            public void onError(Exception ex) {}
        };
        client.connectBlocking();
    }

    public void send(String content) throws Exception {
        if (client == null || !client.isOpen()) {
            throw new IllegalStateException("Chat not connected");
        }
        client.send(mapper.writeValueAsString(java.util.Map.of(
                "content", content,
                "roomId", "general"
        )));
    }

    public void disconnect() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    public boolean isConnected() {
        return client != null && client.isOpen();
    }

    private static String toWebSocketUrl(String apiBaseUrl) {
        String base = apiBaseUrl.replaceAll("/api/?$", "");
        if (base.startsWith("https://")) {
            return "wss://" + base.substring("https://".length());
        }
        if (base.startsWith("http://")) {
            return "ws://" + base.substring("http://".length());
        }
        return "ws://" + base;
    }
}
