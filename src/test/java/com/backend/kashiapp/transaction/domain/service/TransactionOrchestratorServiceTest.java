package com.backend.kashiapp.transaction.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.strategy.TransactionStrategy;
import com.backend.kashiapp.transaction.domain.strategy.TransferStrategy;
import com.backend.kashiapp.transaction.domain.strategy.factory.TransactionStrategyFactory;

@DisplayName("TransactionOrchestratorService")
class TransactionOrchestratorServiceTest {

    private TransactionStrategyFactory strategyFactory;
    private TransactionOrchestratorService orchestrator;

    private final UUID senderId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        strategyFactory = mock(TransactionStrategyFactory.class);
        orchestrator = new TransactionOrchestratorService(strategyFactory);
    }

    @Test
    @DisplayName("Debe pedir la estrategia TRANSFER a la fábrica y devolver su resultado sin cambios")
    void shouldDelegateToTransferStrategy() {
        TransactionStrategy strategy = mock(TransactionStrategy.class);
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail("recipient@example.com");
        request.setAmount(new BigDecimal("50.00"));

        TransactionResponseDTO expected = TransactionResponseDTO.builder()
                .transactionReference(UUID.randomUUID())
                .amount(request.getAmount())
                .status(TransactionStatus.COMPLETED)
                .build();

        when(strategyFactory.getStrategy(TransferStrategy.TYPE)).thenReturn(strategy);
        when(strategy.execute(senderId, request)).thenReturn(expected);

        TransactionResponseDTO result = orchestrator.executeTransfer(senderId, request);

        // Debe pedirse la estrategia TRANSFER a la fábrica y devolver su resultado sin transformarlo.
        verify(strategyFactory).getStrategy(TransferStrategy.TYPE);
        verify(strategy).execute(senderId, request);
        assertThat(result).isSameAs(expected);
    }
}
