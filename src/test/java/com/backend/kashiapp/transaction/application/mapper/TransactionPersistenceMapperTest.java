package com.backend.kashiapp.transaction.application.mapper;

import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import com.backend.kashiapp.transaction.infraestructure.persistence.TransactionEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionPersistenceMapperTest {

    private final UUID id = UUID.randomUUID();
    private final UUID transferReference = UUID.randomUUID();
    private final UUID senderWalletId = UUID.randomUUID();
    private final UUID receiverWalletId = UUID.randomUUID();
    private final BigDecimal amount = new BigDecimal("100.00");
    private final OffsetDateTime createdAt = OffsetDateTime.now();

    @Test
    void toDomain_shouldMapAllFields() {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(id);
        entity.setTransferReference(transferReference);
        entity.setSenderWalletId(senderWalletId);
        entity.setReceiverWalletId(receiverWalletId);
        entity.setAmount(amount);
        entity.setType(TransactionType.OUTGOING);
        entity.setStatus(TransactionStatus.COMPLETED);
        entity.setCreatedAt(createdAt);

        Transaction result = TransactionPersistenceMapper.toDomain(entity);

        assertEquals(id, result.getId());
        assertEquals(transferReference, result.getTransferReference());
        assertEquals(senderWalletId, result.getSenderWalletId());
        assertEquals(receiverWalletId, result.getReceiverWalletId());
        assertEquals(amount, result.getAmount());
        assertEquals(TransactionType.OUTGOING, result.getType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(createdAt, result.getCreatedAt());
    }

    @Test
    void toEntity_shouldMapAllFields() {
        Transaction transaction = Transaction.builder()
                .id(id)
                .transferReference(transferReference)
                .senderWalletId(senderWalletId)
                .receiverWalletId(receiverWalletId)
                .amount(amount)
                .type(TransactionType.INCOMING)
                .status(TransactionStatus.COMPLETED)
                .createdAt(createdAt)
                .build();

        TransactionEntity result = TransactionPersistenceMapper.toEntity(transaction);

        assertEquals(id, result.getId());
        assertEquals(transferReference, result.getTransferReference());
        assertEquals(senderWalletId, result.getSenderWalletId());
        assertEquals(receiverWalletId, result.getReceiverWalletId());
        assertEquals(amount, result.getAmount());
        assertEquals(TransactionType.INCOMING, result.getType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(createdAt, result.getCreatedAt());
    }
}
