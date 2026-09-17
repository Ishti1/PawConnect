package com.catconnect.dto;

import com.catconnect.entity.LostFoundPost.PostType;
import lombok.Data;

@Data
public class LostFoundUpdateRequest {
    private PostType postType;
    private String catDescription;
    private String lastSeenLocation;
    private String contactPhone;
    private String imageUrl;
    private String mapLink;
}

