package com.catconnect.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DonationRequest {
    @NotNull
    @DecimalMin("1.0")
    private BigDecimal amount;
    private String message;
}
