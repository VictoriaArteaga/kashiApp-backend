package com.backend.kashiapp.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteRequestDTO {

    private String email;

    @NotBlank
    private String password;
}
