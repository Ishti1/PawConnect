package com.catconnect.dto;

import lombok.Data;

@Data
public class AdoptionUpdateRequest {
    private String catName;
    private String breed;
    private Integer ageMonths;
    private String gender;
    private String description;
    private String imageUrl;
    private String location;
    private String address;
    private String contactPhone;
}

