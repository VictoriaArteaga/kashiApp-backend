package com.backend.kashiapp.transaction.application.useCase;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;
import com.backend.kashiapp.transaction.domain.service.TransactionService;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.UUID;

// Caso de uso que coordina la ejecución de una transferencia.

@Component
@RequiredArgsConstructor
public class ExecuteTransactionUseCase {

    private final TransactionService
            transactionService;

    private final UserRepository
            userRepository;

    // Recibe el email del token JWT y ejecuta la transferencia.
    public TransactionResponseDTO execute(
            String senderEmail,
            TransactionRequestDTO request
    ) {

        UUID senderUserId = resolveUserId(senderEmail);

        return transactionService.executeTransfer(
                senderUserId,
                request
        );
    }

    // Convierte el email del usuario autenticado a su UUID.
    private UUID resolveUserId(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Usuario con email "
                                        + email
                                        + " no encontrado"
                        )
                );

        return user.getId();
    }

}
