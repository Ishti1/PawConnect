package com.catconnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAccountRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String currentPassword;

    private String newDisplayName;
    private String newPassword;
}
