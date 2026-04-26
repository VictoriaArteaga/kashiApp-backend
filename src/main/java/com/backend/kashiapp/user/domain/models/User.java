package com.backend.kashiapp.user.domain.models;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import lombok.Data;

@Data
public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private String username;
    private OffsetDateTime creationDate;
    private String numberPhone;
    private AccountStatus accountStatus;
}