package com.catconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CampaignRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private BigDecimal goalAmount;
    private Long shelterId;
    private String imageUrl;
    private String accountName;
    private String accountNumber;
    private String bankName;
    private String mobileBanking;
    private String paymentInstructions;
    private String contactPhone;
}
