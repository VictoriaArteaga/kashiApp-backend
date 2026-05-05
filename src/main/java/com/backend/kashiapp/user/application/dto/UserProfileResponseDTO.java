package com.backend.kashiapp.user.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.backend.kashiapp.user.domain.models.enums.AccountStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponseDTO {
    private UUID id;
    private String email;
    private String username;
    private String numberPhone;
    private AccountStatus accountStatus;
    private OffsetDateTime createdAt;

}