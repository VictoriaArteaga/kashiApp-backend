package com.backend.kashiapp.transaction.domain.service;

import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;
import com.backend.kashiapp.transaction.domain.strategy.TransferStrategy;
import com.backend.kashiapp.transaction.domain.strategy.factory.TransactionStrategyFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.UUID;

// Orquestador que delega la ejecución a la estrategia correspondiente.
// Actúa como punto de entrada del dominio para cualquier tipo de transacción.
@Service
@RequiredArgsConstructor
public class TransactionOrchestratorService implements TransactionService {

    private final TransactionStrategyFactory
            strategyFactory;

    @Override
    public TransactionResponseDTO executeTransfer(
            UUID senderUserId,
            TransactionRequestDTO request
    ) {

        // Busca la estrategia de transferencia y la ejecuta
        return strategyFactory
                .getStrategy(TransferStrategy.TYPE)
                .execute(senderUserId, request);
    }
}