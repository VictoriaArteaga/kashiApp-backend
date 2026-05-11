package com.backend.kashiapp.wallet.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class WalletResponseDTO {
    private UUID id;
    private BigDecimal balance;
    private boolean visible;
    private OffsetDateTime updatedAt;
}
