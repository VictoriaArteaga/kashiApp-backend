package com.backend.kashiapp.user.application.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private UUID id;
    private String email;
    private String username;
    private String numberPhone;
    private AccountStatus accountStatus;

}
