package com.catconnect.dto;

import com.catconnect.entity.LostFoundPost.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LostFoundRequest {
    @NotNull
    private PostType postType;
    @NotBlank
    private String catDescription;
    private String lastSeenLocation;
    private String contactPhone;
    private String imageUrl;
    private String mapLink;
}
