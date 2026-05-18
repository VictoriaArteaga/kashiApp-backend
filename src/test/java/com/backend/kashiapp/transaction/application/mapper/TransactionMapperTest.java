package com.backend.kashiapp.transaction.application.mapper;

import com.backend.kashiapp.transaction.application.dto.TransactionHistoryResponseDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionHistoryType;
import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.backend.kashiapp.transaction.infraestructure.persistence.TransactionEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


@DisplayName("TransactionMapper")
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
    @DisplayName("Debe mapear cada campo de la entidad al modelo de dominio")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID reference = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("250.75");
        OffsetDateTime now = OffsetDateTime.now();

        TransactionEntity entity = new TransactionEntity();
        entity.setId(id);
        entity.setTransferReference(reference);
        entity.setSenderWalletId(senderWalletId);
        entity.setReceiverWalletId(receiverWalletId);
        entity.setAmount(amount);
        entity.setType(TransactionType.OUTGOING);
        entity.setStatus(TransactionStatus.COMPLETED);
        entity.setCreatedAt(now);

        Transaction domain = TransactionMapper.toDomain(entity);

        // Comparación recursiva: los nombres de campo coinciden entre Transaction y TransactionEntity.
        assertThat(domain).usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    @DisplayName("Debe mapear cada campo del dominio a la entidad lista para persistir")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID reference = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("99.99");
        OffsetDateTime now = OffsetDateTime.now();

        Transaction domain = Transaction.builder()
                .id(id)
                .transferReference(reference)
                .senderWalletId(senderWalletId)
                .receiverWalletId(receiverWalletId)
                .amount(amount)
                .type(TransactionType.INCOMING)
                .status(TransactionStatus.COMPLETED)
                .createdAt(now)
                .build();

        TransactionEntity entity = TransactionMapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTransferReference()).isEqualTo(reference);
        assertThat(entity.getSenderWalletId()).isEqualTo(senderWalletId);
        assertThat(entity.getReceiverWalletId()).isEqualTo(receiverWalletId);
        assertThat(entity.getAmount()).isEqualByComparingTo(amount);
        assertThat(entity.getType()).isEqualTo(TransactionType.INCOMING);
        assertThat(entity.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
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

    @Test
    @DisplayName("Debe conservar todos los valores en un round trip dominio -> entidad -> dominio")
    void shouldPreserveAllValuesOnRoundTrip() {
        // Un ciclo dominio -> entidad -> dominio no debe perder información.
        Transaction original = Transaction.builder()
                .id(UUID.randomUUID())
                .transferReference(UUID.randomUUID())
                .senderWalletId(UUID.randomUUID())
                .receiverWalletId(UUID.randomUUID())
                .amount(new BigDecimal("1234.56"))
                .type(TransactionType.OUTGOING)
                .status(TransactionStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        Transaction roundTripped = TransactionMapper.toDomain(TransactionMapper.toEntity(original));

        // El objeto reconstruido debe ser igual al original campo a campo.
        assertThat(roundTripped).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("Debe propagar id y createdAt nulos sin lanzar NullPointerException")
    void shouldMapNullableFieldsWithoutThrowing() {
        // Las transacciones recién construidas pueden no tener id/createdAt todavía (los asigna JPA al persistir).
        TransactionEntity entity = new TransactionEntity();
        entity.setTransferReference(UUID.randomUUID());
        entity.setSenderWalletId(UUID.randomUUID());
        entity.setReceiverWalletId(UUID.randomUUID());
        entity.setAmount(new BigDecimal("10.00"));
        entity.setType(TransactionType.OUTGOING);
        entity.setStatus(TransactionStatus.PENDING);

        Transaction domain = TransactionMapper.toDomain(entity);

        // Un id y createdAt en null deben propagarse como null en vez de provocar un NPE.
        assertThat(domain.getId()).isNull();
        assertThat(domain.getCreatedAt()).isNull();
    }
}
