package com.backend.kashiapp.wallet.application.useCase;

import com.backend.kashiapp.wallet.domain.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("DepositMoneyUseCase")
class DepositMoneyUseCaseTest {

    private WalletService walletService;
    private DepositMoneyUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        walletService = mock(WalletService.class);
        useCase = new DepositMoneyUseCase(walletService);
    }

    @Test
    @DisplayName("Debe llamar a updateBalance con el monto positivo dado")
    void shouldCallUpdateBalanceWithPositiveAmount() {
        BigDecimal amount = new BigDecimal("100.00");

        useCase.execute(userId, amount);

        verify(walletService).updateBalance(userId, amount);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el monto es cero")
    void shouldThrowWhenAmountIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(userId, BigDecimal.ZERO));

        verify(walletService, never()).updateBalance(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el monto es negativo")
    void shouldThrowWhenAmountIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(userId, new BigDecimal("-50.00")));

        verify(walletService, never()).updateBalance(any(), any());
    }

    @Test
    @DisplayName("Debe aceptar montos con decimales")
    void shouldAcceptDecimalAmount() {
        BigDecimal amount = new BigDecimal("0.01");

        useCase.execute(userId, amount);

        verify(walletService).updateBalance(userId, amount);
    }
}
