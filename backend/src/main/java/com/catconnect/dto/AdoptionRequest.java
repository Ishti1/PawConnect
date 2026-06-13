package com.catconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdoptionRequest {
    @NotBlank
    private String catName;
    private String breed;
    private Integer ageMonths;
    private String gender;
    private String description;
    private String imageUrl;
    private Long shelterId;
}
