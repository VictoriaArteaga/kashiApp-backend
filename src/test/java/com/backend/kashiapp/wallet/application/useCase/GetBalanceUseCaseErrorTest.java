package com.backend.kashiapp.wallet.application.useCase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.backend.kashiapp.wallet.domain.service.WalletService;

@DisplayName("GetBalanceUseCase Error Tests")
class GetBalanceUseCaseErrorTest {
    private WalletService walletService;
    private GetBalanceUseCase getBalanceUseCase;

    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setup() {
        walletService = mock(WalletService.class);
        getBalanceUseCase = new GetBalanceUseCase(walletService);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la wallet no existe")
    void shouldThrowExceptionWhenWalletNotFound() {
        // Simulación de que la wallet no existe
        when(walletService.getWalletByUserId(USER_ID))
                .thenThrow(new RuntimeException("Wallet no encontrada para el usuario"));

        // Se espera que se lance una excepción
        Exception ex = assertThrows(
                RuntimeException.class,
                () -> getBalanceUseCase.execute(USER_ID)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("wallet"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando hay error en el servicio")
    void shouldThrowExceptionWhenServiceError() {
        // Simulación de error del servicio
        when(walletService.getWalletByUserId(USER_ID))
                .thenThrow(new RuntimeException("Error de conexión a la base de datos"));

        // Se espera que se lance una excepción
        Exception ex = assertThrows(
                RuntimeException.class,
                () -> getBalanceUseCase.execute(USER_ID)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("error"));
    }
}
