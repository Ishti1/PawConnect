package com.catconnect.repository;

import com.catconnect.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop100ByRoomIdOrderBySentAtDesc(String roomId);
}
