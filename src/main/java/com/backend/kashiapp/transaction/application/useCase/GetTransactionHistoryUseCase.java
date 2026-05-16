package com.backend.kashiapp.transaction.application.useCase;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.transaction.application.dto.TransactionHistoryResponseDTO;
import com.backend.kashiapp.transaction.application.mapper.TransactionMapper;
import com.backend.kashiapp.transaction.domain.repository.TransactionRepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.wallet.domain.service.WalletService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

// Caso de uso que devuelve el historial de movimientos del usuario autenticado.
@Component
@RequiredArgsConstructor
public class GetTransactionHistoryUseCase {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final UserRepository userRepository;

    public List<TransactionHistoryResponseDTO> execute(String email) {

        UUID userId = resolveUserId(email);

        // El walletId del usuario se obtiene desde su billetera registrada
        UUID walletId = walletService.getWalletByUserId(userId).getId();

        return transactionRepository
                .findHistoryByWalletId(walletId)
                .stream()
                .map(TransactionMapper::toHistoryDTO)
                .toList();
    }

    // Convierte el email del token JWT al UUID del usuario.
    private UUID resolveUserId(String email) {

        UserEntity user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Usuario con email " + email + " no encontrado"
                        )
                );

        return user.getId();
    }
}
