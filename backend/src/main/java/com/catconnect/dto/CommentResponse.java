package com.catconnect.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String content;
    private LocalDateTime createdAt;
}
