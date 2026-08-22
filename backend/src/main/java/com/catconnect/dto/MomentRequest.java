package com.catconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MomentRequest {
    private String caption;
    private String imageUrl;
    private String mediaType;
    private Long sharedMomentId;
}
