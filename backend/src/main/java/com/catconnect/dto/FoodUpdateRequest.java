package com.catconnect.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodUpdateRequest {
    private String brand;
    private String productName;
    private String ageGroup;
    private String healthCondition;
    private String description;
    private BigDecimal rating;
    private String imageUrl;
}
