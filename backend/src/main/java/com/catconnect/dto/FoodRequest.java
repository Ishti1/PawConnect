package com.catconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodRequest {
    @NotBlank
    private String brand;
    @NotBlank
    private String productName;
    @NotBlank
    private String ageGroup;
    private String healthCondition;
    private String description;
    private BigDecimal rating;
    private String imageUrl;
}
