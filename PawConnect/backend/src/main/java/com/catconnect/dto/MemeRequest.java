package com.catconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemeRequest {
    private String title;
    @NotBlank
    private String imageUrl;
}
