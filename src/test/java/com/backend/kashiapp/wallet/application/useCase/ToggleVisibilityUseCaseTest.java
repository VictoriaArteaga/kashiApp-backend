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

@DisplayName("ToggleVisibilityUseCase")
class ToggleVisibilityUseCaseTest {

    private WalletService walletService;
    private ToggleVisibilityUseCase toggleVisibilityUseCase;

    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setup() {
        walletService = mock(WalletService.class);
        toggleVisibilityUseCase = new ToggleVisibilityUseCase(walletService);
    }

    @Nested
    @DisplayName("Cuando el cambio de visibilidad es exitoso")
    class CambioExitoso {

        @Test
        @DisplayName("debe retornar la wallet con visibilidad desactivada cuando estaba activa")
        void shouldReturnInvisibleWhenWasVisible() {
            // Se prepara una wallet que actualmente está visible y tras el toggle quedará oculta
            WalletResponseDTO walletAfterToggle = buildWallet("500.00", false);
            when(walletService.toggleVisibility(USER_ID)).thenReturn(walletAfterToggle);

            // Se ejecuta el caso de uso
            WalletResponseDTO result = toggleVisibilityUseCase.execute(USER_ID);

            // Se verifica que la wallet ahora está oculta y el servicio fue llamado
            assertNotNull(result);
            assertEquals(false, result.isVisible());
            verify(walletService).toggleVisibility(USER_ID);
        }

        @Test
        @DisplayName("debe retornar la wallet con visibilidad activa cuando estaba desactivada")
        void shouldReturnVisibleWhenWasInvisible() {
            // Se prepara una wallet que actualmente está oculta y tras el toggle quedará visible
            WalletResponseDTO walletAfterToggle = buildWallet("750.00", true);
            when(walletService.toggleVisibility(USER_ID)).thenReturn(walletAfterToggle);

            // Se ejecuta el caso de uso
            WalletResponseDTO result = toggleVisibilityUseCase.execute(USER_ID);

            // Se verifica que la wallet ahora es visible y el servicio fue llamado
            assertNotNull(result);
            assertEquals(true, result.isVisible());
            verify(walletService).toggleVisibility(USER_ID);
        }

        @Test
        @DisplayName("debe retornar la wallet con el timestamp actualizado")
        void shouldReturnWalletWithUpdatedTimestamp() {
            // Se prepara una wallet con timestamp reciente después del toggle
            WalletResponseDTO walletAfterToggle = buildWallet("1000.00", true);
            when(walletService.toggleVisibility(USER_ID)).thenReturn(walletAfterToggle);

            // Se ejecuta el caso de uso
            WalletResponseDTO result = toggleVisibilityUseCase.execute(USER_ID);

            // Se verifica que el timestamp no es nulo
            assertNotNull(result);
            assertNotNull(result.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Cuando ocurre un error")
    class ErrorEnToggle {

        @Test
        @DisplayName("debe propagar la excepción cuando la wallet no existe")
        void shouldPropagateExceptionWhenWalletNotFound() {
            // Simulación de que la wallet no existe para ese usuario
            when(walletService.toggleVisibility(USER_ID))
                    .thenThrow(new RuntimeException("Wallet no encontrada para el usuario"));

            // Se ejecuta el caso de uso y se espera que lance la excepción
            Exception ex = assertThrows(
                    RuntimeException.class,
                    () -> toggleVisibilityUseCase.execute(USER_ID)
            );

            // Se verifica que la excepción fue propagada correctamente
            assertNotNull(ex.getMessage());
        }
    }

    // ---------------------

    private WalletResponseDTO buildWallet(String balance, boolean visible) {
        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(UUID.randomUUID());
        dto.setBalance(new BigDecimal(balance));
        dto.setVisible(visible);
        dto.setUpdatedAt(OffsetDateTime.now());
        return dto;
    }
}