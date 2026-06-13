package com.catconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MomentRequest {
    private String caption;
    @NotBlank
    private String imageUrl;
}
