package com.backend.kashiapp.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

// DTO para la solicitud de registro de usuario
@Data
public class UserRequestDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String username;

    @NotBlank
    private String numberPhone;

    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "El número de identificación debe contener solo dígitos")
    private String identificationNumber;


}
