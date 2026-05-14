package com.backend.kashiapp.wallet.application.useCase;

import com.backend.kashiapp.wallet.domain.service.WalletService;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DepositMoneyUseCase {
    private final WalletService walletService;

    public DepositMoneyUseCase(WalletService walletService) {
        this.walletService = walletService;
    }

    public void execute(UUID userId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        walletService.updateBalance(userId, amount);
    }
}
