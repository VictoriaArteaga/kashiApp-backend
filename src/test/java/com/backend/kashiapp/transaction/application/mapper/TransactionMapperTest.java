package com.backend.kashiapp.transaction.application.mapper;

import com.backend.kashiapp.transaction.application.dto.TransactionHistoryResponseDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionHistoryType;
import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private Transaction buildTransaction(TransactionType type) {
        return Transaction.builder()
                .transferReference(UUID.randomUUID())
                .amount(new BigDecimal("50.00"))
                .type(type)
                .status(TransactionStatus.COMPLETED)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void shouldMapTransferReference() {
        UUID ref = UUID.randomUUID();
        Transaction tx = Transaction.builder()
                .transferReference(ref)
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.OUTGOING)
                .status(TransactionStatus.COMPLETED)
                .createdAt(OffsetDateTime.now())
                .build();

        assertEquals(ref, TransactionMapper.toHistoryDTO(tx).getTransferReference());
    }

    @Test
    void shouldMapAmount() {
        Transaction tx = Transaction.builder()
                .transferReference(UUID.randomUUID())
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.OUTGOING)
                .status(TransactionStatus.COMPLETED)
                .createdAt(OffsetDateTime.now())
                .build();

        assertEquals(new BigDecimal("50.00"), TransactionMapper.toHistoryDTO(tx).getAmount());
    }

    @Test
    void shouldMapCreatedAtToDate() {
        OffsetDateTime now = OffsetDateTime.now();
        Transaction tx = Transaction.builder()
                .transferReference(UUID.randomUUID())
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.OUTGOING)
                .status(TransactionStatus.COMPLETED)
                .createdAt(now)
                .build();

        assertEquals(now, TransactionMapper.toHistoryDTO(tx).getDate());
    }

    @Test
    void shouldMapOutgoingTypeToSent() {
        TransactionHistoryResponseDTO dto = TransactionMapper.toHistoryDTO(buildTransaction(TransactionType.OUTGOING));
        assertEquals(TransactionHistoryType.SENT, dto.getType());
    }

    @Test
    void shouldMapIncomingTypeToReceived() {
        TransactionHistoryResponseDTO dto = TransactionMapper.toHistoryDTO(buildTransaction(TransactionType.INCOMING));
        assertEquals(TransactionHistoryType.RECEIVED, dto.getType());
    }
}
