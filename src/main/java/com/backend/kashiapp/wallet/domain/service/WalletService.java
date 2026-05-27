package com.backend.kashiapp.wallet.domain.service;

import java.math.BigDecimal;
import java.util.UUID;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;

public interface WalletService {
    WalletResponseDTO getWalletByUserId(UUID userId);
    WalletResponseDTO toggleVisibility(UUID userId);
    void updateBalance(UUID userId, BigDecimal amount);

    // Toma un bloqueo pesimista sobre las billeteras de ambos usuarios dentro de la
    // transacción en curso. Los locks se adquieren en un orden global consistente (por UUID)
    // para que dos transferencias inversas (A->B y B->A) simultáneas no se interbloqueen.
    // Además serializa las operaciones del mismo emisor (necesario para deduplicar duplicados).
    void lockWalletsForUpdate(UUID userIdA, UUID userIdB);
}
