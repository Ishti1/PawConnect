package com.catconnect.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VetUpdateRequest {
    private String name;
    private String address;
    private String phone;
    private BigDecimal rating;
    private Boolean emergency;
    private String openHours;
    private String city;
    private String imageUrl;
    private String mapLink;
}
