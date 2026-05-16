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

    // "SENT" si el usuario envió el dinero, "RECEIVED" si lo recibió.
    private String type;

    private BigDecimal amount;

    private OffsetDateTime date;
}
