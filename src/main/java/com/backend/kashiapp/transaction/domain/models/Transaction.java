package com.backend.kashiapp.transaction.domain.models;

import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Transaction {

    private UUID id;

    // Identificador compartido entre la transacción OUTGOING e INCOMING.
    private UUID transferReference;

    private UUID senderWalletId;
    private UUID receiverWalletId;
    private BigDecimal amount;
    private TransactionStatus status;
    private TransactionType type;
    private OffsetDateTime createdAt;
}
