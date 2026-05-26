package com.backend.kashiapp.transaction.domain.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

// Evento que se dispara cuando una transferencia se completa exitosamente.
// Se publica dentro del @Transactional y se consume solo después del commit.
@Getter
@RequiredArgsConstructor
public class TransferCompletedEvent {

    private final UUID senderUserId;
    private final UUID recipientUserId;
    private final BigDecimal amount;
    private final UUID transferReference;
}
