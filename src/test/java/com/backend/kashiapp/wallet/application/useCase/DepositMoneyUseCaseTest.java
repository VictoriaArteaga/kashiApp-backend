package com.backend.kashiapp.wallet.application.useCase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.kashiapp.wallet.domain.service.WalletService;

@DisplayName("DepositMoneyUseCase")
class DepositMoneyUseCaseTest {

    private WalletService walletService;
    private DepositMoneyUseCase depositMoneyUseCase;

    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setup() {
        walletService = mock(WalletService.class);
        depositMoneyUseCase = new DepositMoneyUseCase(walletService);
    }

    @Nested
    @DisplayName("Cuando el depósito es exitoso")
    class DepositoExitoso {

        @Test
        @DisplayName("debe depositar un monto válido y actualizar el balance")
        void shouldDepositValidAmount() {
            // Se prepara un monto válido para depositaar
            BigDecimal amount = new BigDecimal("100.00");

            // Se ejecuta el caso de uso con el monto válido
            assertDoesNotThrow(() -> depositMoneyUseCase.execute(USER_ID, amount));

            // Se verifica que el balance fue actualizado correctamente
            verify(walletService).updateBalance(USER_ID, amount);
        }

        @Test
        @DisplayName("debe depositar un monto grande correctamente")
        void shouldDepositLargeAmount() {
            // Se prepara un monto grande para depositar
            BigDecimal largeAmount = new BigDecimal("999999.99");

            // Se ejecuta el caso de uso con el monto grande
            assertDoesNotThrow(() -> depositMoneyUseCase.execute(USER_ID, largeAmount));

            // Se verifica que el balance fue actualizado correctamente
            verify(walletService).updateBalance(USER_ID, largeAmount);
        }

        @Test
        @DisplayName("debe depositar un monto mínimo positivo")
        void shouldDepositMinimalPositiveAmount() {
            // Se prepara el monto mínimo permitido
            BigDecimal minAmount = new BigDecimal("0.01");

            // Se ejecuta el caso de uso con el monto mínimo
            assertDoesNotThrow(() -> depositMoneyUseCase.execute(USER_ID, minAmount));

            // Se verifica que el balance fue actualizado correctamente
            verify(walletService).updateBalance(USER_ID, minAmount);
        }
    }

    @Nested
    @DisplayName("Cuando el monto es inválido")
    class MontoInvalido {

        @Test
        @DisplayName("debe lanzar excepción cuando el monto es cero")
        void shouldThrowExceptionWhenAmountIsZero() {
            // Se espera que se lance una excepción cuando el monto es cero
            Exception ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> depositMoneyUseCase.execute(USER_ID, BigDecimal.ZERO)
            );

            // Se verifica el mensaje y que el servicio nunca fue llamado
            assertEquals("El monto debe ser mayor a cero", ex.getMessage());
            verify(walletService, never()).updateBalance(any(), any());
        }

        @Test
        @DisplayName("debe lanzar excepción cuando el monto es negativo")
        void shouldThrowExceptionWhenAmountIsNegative() {
            // Se espera que se lance una excepción cuando el monto es negativo
            Exception ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> depositMoneyUseCase.execute(USER_ID, new BigDecimal("-50.00"))
            );

            // Se verifica el mensaje y que el servicio nunca fue llamado
            assertEquals("El monto debe ser mayor a cero", ex.getMessage());
            verify(walletService, never()).updateBalance(any(), any());
        }
    }
}