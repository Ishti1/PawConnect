package com.catconnect.dto;

import com.catconnect.entity.ChatMessage;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageDto {

    private Long id; // Add this
    private Long senderId;
    private String senderName;
    private String content;
    private String roomId;
    private LocalDateTime sentAt; // Add this

    public static ChatMessageDto from(ChatMessage m) {
        return ChatMessageDto.builder()
                .id(m.getId())
                .senderId(m.getSenderId())
                .senderName(m.getSenderName())
                .content(m.getContent())
                .roomId(m.getRoomId())
                .sentAt(m.getSentAt())
                .build();
    }
}