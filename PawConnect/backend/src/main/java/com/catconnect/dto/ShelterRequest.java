package com.catconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShelterRequest {
    @NotBlank
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String description;
    private Integer capacity;
    private String city;
    private String imageUrl;
    private String mapLink;
}
