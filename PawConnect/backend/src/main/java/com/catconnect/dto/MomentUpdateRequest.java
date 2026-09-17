package com.catconnect.dto;

import lombok.Data;

@Data
public class MomentUpdateRequest {
    private String caption;
    private String imageUrl;
    private String mediaType;
}

