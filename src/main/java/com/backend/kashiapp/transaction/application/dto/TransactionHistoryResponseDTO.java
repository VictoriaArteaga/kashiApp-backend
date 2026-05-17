package com.backend.kashiapp.transaction.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class TransactionHistoryResponseDTO {

    // Identificador compartido entre los dos registros de una misma transferencia.
    private UUID transferReference;

    private TransactionHistoryType type;

    private BigDecimal amount;

    private OffsetDateTime date;
}
