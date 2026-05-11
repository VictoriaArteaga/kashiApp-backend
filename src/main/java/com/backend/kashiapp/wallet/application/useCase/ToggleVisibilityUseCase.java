package com.backend.kashiapp.wallet.application.useCase;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.service.WalletService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ToggleVisibilityUseCase {
    private final WalletService walletService;

    public ToggleVisibilityUseCase(WalletService walletService) {
        this.walletService = walletService;
    }

    public WalletResponseDTO execute(UUID userId) {
        return walletService.toggleVisibility(userId);
    }
}
