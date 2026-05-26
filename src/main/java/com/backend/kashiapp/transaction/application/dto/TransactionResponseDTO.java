package com.backend.kashiapp.transaction.application.dto;

import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

// Respuesta que se devuelve al cliente después de ejecutar una transferencia.
@Getter
@Builder
public class TransactionResponseDTO {

    // Referencia única compartida entre ambas transacciones (OUTGOING e INCOMING).
    private UUID transactionReference;
    private BigDecimal amount;
    private TransactionStatus status;
}
