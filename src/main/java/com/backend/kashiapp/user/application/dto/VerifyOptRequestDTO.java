package com.backend.kashiapp.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOptRequestDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String otp;
    
}
