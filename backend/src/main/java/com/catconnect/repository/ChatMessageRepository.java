package com.catconnect.repository;

import com.catconnect.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop100ByRoomIdOrderBySentAtDesc(String roomId);

    List<ChatMessage> findTop100ByRoomIdInOrderBySentAtDesc(Collection<String> roomIds);

    @Query("""
            SELECT DISTINCT m.roomId FROM ChatMessage m
            WHERE m.roomId LIKE 'dm_%'
            AND (
                m.senderId = :userId
                OR m.roomId LIKE CONCAT('dm_', :userId, '_%')
                OR m.roomId LIKE CONCAT('dm_%_', :userId)
                OR m.roomId = CONCAT('dm_', :userId)
            )
            """)
    List<String> findDmRoomIdsForUser(@Param("userId") Long userId);
}
