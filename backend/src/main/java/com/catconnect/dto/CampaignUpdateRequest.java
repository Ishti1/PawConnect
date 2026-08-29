package com.catconnect.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CampaignUpdateRequest {
    private String title;
    private String description;
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
