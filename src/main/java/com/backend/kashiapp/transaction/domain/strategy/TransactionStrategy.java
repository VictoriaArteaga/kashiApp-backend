package com.backend.kashiapp.transaction.domain.strategy;

import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;

import java.util.UUID;

// Interfaz del patrón Strategy para manejar distintos tipos de transacción.
// Cada implementación define su tipo y su lógica de ejecución.
public interface TransactionStrategy {

    // Identifica qué tipo de transacción maneja esta estrategia.
    String getType();

    TransactionResponseDTO execute(
            UUID senderUserId,
            TransactionRequestDTO request
    );
}
