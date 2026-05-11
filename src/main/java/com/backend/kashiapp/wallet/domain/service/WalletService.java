package com.backend.kashiapp.wallet.domain.service;

import java.math.BigDecimal;
import java.util.UUID;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;

public interface WalletService {
    WalletResponseDTO getWalletByUserId(UUID userId);
    WalletResponseDTO toggleVisibility(UUID userId);
    void updateBalance(UUID userId, BigDecimal amount);
}
