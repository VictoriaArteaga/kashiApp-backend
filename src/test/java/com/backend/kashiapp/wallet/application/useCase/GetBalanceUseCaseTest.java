package com.backend.kashiapp.wallet.application.useCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.service.WalletService;

@DisplayName("GetBalanceUseCase")
class GetBalanceUseCaseTest {

    private WalletService walletService;
    private GetBalanceUseCase getBalanceUseCase;

    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setup() {
        walletService = mock(WalletService.class);
        getBalanceUseCase = new GetBalanceUseCase(walletService);
    }

    @Nested
    @DisplayName("Cuando la consulta es exitosa")
    class ConsultaExitosa {

        @Test
        @DisplayName("debe retornar la wallet con su balance correctamente")
        void shouldReturnWalletWithBalance() {
            // Se prepara la wallet esperada con balance y visibilidad
            WalletResponseDTO expected = buildWallet("500.00", true);
            when(walletService.getWalletByUserId(USER_ID)).thenReturn(expected);

            // Se ejecuta el caso de uso
            WalletResponseDTO result = getBalanceUseCase.execute(USER_ID);

            // Se verifica que la wallet retornada tiene los datos correctos
            assertNotNull(result);
            assertEquals(expected.getBalance(), result.getBalance());
            assertEquals(expected.isVisible(), result.isVisible());
            verify(walletService).getWalletByUserId(USER_ID);
        }

        @Test
        @DisplayName("debe retornar wallet con balance en cero")
        void shouldReturnWalletWithZeroBalance() {
            // Se prepara una wallet con balance en cero
            WalletResponseDTO expected = buildWallet("0.00", true);
            when(walletService.getWalletByUserId(USER_ID)).thenReturn(expected);

            // Se ejecuta el caso de uso
            WalletResponseDTO result = getBalanceUseCase.execute(USER_ID);

            // Se verifica que el balance retornado es cero
            assertNotNull(result);
            assertEquals(new BigDecimal("0.00"), result.getBalance());
        }

        @Test
        @DisplayName("debe retornar wallet con visibilidad desactivada")
        void shouldReturnInvisibleWallet() {
            // Se prepara una wallet que está oculta para el usuario
            WalletResponseDTO expected = buildWallet("1000.00", false);
            when(walletService.getWalletByUserId(USER_ID)).thenReturn(expected);

            // Se ejecuta el caso de uso
            WalletResponseDTO result = getBalanceUseCase.execute(USER_ID);

            // Se verifica que la wallet retornada no es visible
            assertNotNull(result);
            assertEquals(false, result.isVisible());
        }
    }

    @Nested
    @DisplayName("Cuando ocurre un error")
    class ErrorEnConsulta {

        @Test
        @DisplayName("debe propagar la excepción cuando la wallet no existe")
        void shouldPropagateExceptionWhenWalletNotFound() {
            // Simulación de que la wallet no existe para ese usuario
            when(walletService.getWalletByUserId(USER_ID))
                    .thenThrow(new RuntimeException("Wallet no encontrada para el usuario"));

            // Se ejecuta el caso de uso y se espera que lance la excepción
            Exception ex = assertThrows(
                    RuntimeException.class,
                    () -> getBalanceUseCase.execute(USER_ID)
            );

            // Se verifica que la excepción fue propagada correctamente
            assertNotNull(ex.getMessage());
        }

        @Test
        @DisplayName("debe propagar la excepción cuando el servicio falla")
        void shouldPropagateExceptionWhenServiceFails() {
            // Simulación de un error interno en el servicio
            when(walletService.getWalletByUserId(USER_ID))
                    .thenThrow(new RuntimeException("Error de conexión a la base de datos"));

            // Se ejecuta el caso de uso y se espera que lance la excepción
            Exception ex = assertThrows(
                    RuntimeException.class,
                    () -> getBalanceUseCase.execute(USER_ID)
            );

            // Se verifica que la excepción fue propagada correctamente
            assertNotNull(ex.getMessage());
        }
    }

    // --------
    private WalletResponseDTO buildWallet(String balance, boolean visible) {
        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(UUID.randomUUID());
        dto.setBalance(new BigDecimal(balance));
        dto.setVisible(visible);
        dto.setUpdatedAt(OffsetDateTime.now());
        return dto;
    }
}