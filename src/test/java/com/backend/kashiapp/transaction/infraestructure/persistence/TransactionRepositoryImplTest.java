package com.backend.kashiapp.transaction.infraestructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;

@DisplayName("TransactionRepositoryImpl")
class TransactionRepositoryImplTest {

    private JpaTransactionRepository jpaTransactionRepository;
    private TransactionRepositoryImpl repository;

    private final UUID reference = UUID.randomUUID();
    private final UUID senderWalletId = UUID.randomUUID();
    private final UUID receiverWalletId = UUID.randomUUID();
    private final BigDecimal amount = new BigDecimal("42.42");

    @BeforeEach
    void setup() {
        jpaTransactionRepository = mock(JpaTransactionRepository.class);
        repository = new TransactionRepositoryImpl(jpaTransactionRepository);
    }

    // Construye un dominio sin id ni createdAt (los asigna JPA al persistir).
    private Transaction buildDomainToSave() {
        return Transaction.builder()
                .transferReference(reference)
                .senderWalletId(senderWalletId)
                .receiverWalletId(receiverWalletId)
                .amount(amount)
                .type(TransactionType.OUTGOING)
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    // Simula la entidad devuelta por JPA tras el save (ya con id y createdAt poblados).
    private TransactionEntity buildPersistedEntity() {
        TransactionEntity persisted = new TransactionEntity();
        persisted.setId(UUID.randomUUID());
        persisted.setTransferReference(reference);
        persisted.setSenderWalletId(senderWalletId);
        persisted.setReceiverWalletId(receiverWalletId);
        persisted.setAmount(amount);
        persisted.setType(TransactionType.OUTGOING);
        persisted.setStatus(TransactionStatus.COMPLETED);
        persisted.setCreatedAt(OffsetDateTime.now());
        return persisted;
    }

    @Test
    @DisplayName("Debe pasar a JPA una entidad con todos los campos copiados del dominio")
    void shouldMapDomainFieldsToEntityBeforeSaving() {
        when(jpaTransactionRepository.save(any(TransactionEntity.class)))
                .thenReturn(buildPersistedEntity());

        repository.save(buildDomainToSave());

        // Capturamos la entidad que realmente se pasó a JPA y verificamos que todos los campos vienen del dominio.
        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(jpaTransactionRepository).save(captor.capture());

        TransactionEntity captured = captor.getValue();
        assertThat(captured.getTransferReference()).isEqualTo(reference);
        assertThat(captured.getSenderWalletId()).isEqualTo(senderWalletId);
        assertThat(captured.getReceiverWalletId()).isEqualTo(receiverWalletId);
        assertThat(captured.getAmount()).isEqualByComparingTo(amount);
        assertThat(captured.getType()).isEqualTo(TransactionType.OUTGOING);
        assertThat(captured.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe devolver el dominio reflejando el estado persistido (id y createdAt)")
    void shouldReturnDomainReflectingPersistedState() {
        TransactionEntity persisted = buildPersistedEntity();
        when(jpaTransactionRepository.save(any(TransactionEntity.class))).thenReturn(persisted);

        Transaction result = repository.save(buildDomainToSave());

        // El objeto de dominio devuelto debe reflejar el estado persistido incluyendo el id generado.
        assertThat(result.getId()).isEqualTo(persisted.getId());
        assertThat(result.getCreatedAt()).isEqualTo(persisted.getCreatedAt());
        assertThat(result.getTransferReference()).isEqualTo(reference);
    }
}
