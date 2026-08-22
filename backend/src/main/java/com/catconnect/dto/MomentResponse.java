package com.catconnect.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MomentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String caption;
    private String imageUrl;
    private String mediaType;
    private Integer likes;
    private LocalDateTime createdAt;
    
    // For reposts
    private MomentResponse sharedMoment;
}
