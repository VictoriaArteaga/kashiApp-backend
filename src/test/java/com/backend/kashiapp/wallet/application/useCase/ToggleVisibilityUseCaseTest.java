package com.backend.kashiapp.wallet.application.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.kashiapp.common.exception.InvalidWalletBalanceException;
import com.backend.kashiapp.common.exception.InvalidWalletStateException;
import com.backend.kashiapp.common.exception.WalletNotFoundException;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.service.WalletService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del caso de uso ToggleVisibilityUseCase")
class ToggleVisibilityUseCaseTest {

    @Mock
    private WalletService walletService;

    private ToggleVisibilityUseCase toggleVisibilityUseCase;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final BigDecimal BALANCE = new BigDecimal("2000.00");

    @BeforeEach
    void setup() {
        toggleVisibilityUseCase = new ToggleVisibilityUseCase(walletService);
    }

    @Nested
    @DisplayName("Toggle exitoso")
    class SuccessfulToggleTests {

        @Test
        @DisplayName("Debe alternar la visibilidad correctamente")
        void shouldToggleVisibilityCorrectly() {
            WalletResponseDTO responseDTO = buildWalletResponseDTO(false);
            when(walletService.toggleVisibility(USER_ID)).thenReturn(responseDTO);

            WalletResponseDTO result = toggleVisibilityUseCase.execute(USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.isVisible()).isFalse();
            verify(walletService).toggleVisibility(USER_ID);
        }

        @Test
        @DisplayName("Debe preservar el balance durante el toggle")
        void shouldPreserveBalanceDuringToggle() {
            WalletResponseDTO responseDTO = buildWalletResponseDTO(false);
            when(walletService.toggleVisibility(USER_ID)).thenReturn(responseDTO);

            WalletResponseDTO result = toggleVisibilityUseCase.execute(USER_ID);

            assertThat(result.getBalance()).isEqualTo(BALANCE);
        }

        @Test
        @DisplayName("Debe manejar balance negativo")
        void shouldHandleNegativeBalance() {
            WalletResponseDTO responseDTO = buildWalletResponseDTO(false);
            responseDTO.setBalance(new BigDecimal("-500.00"));
            when(walletService.toggleVisibility(USER_ID)).thenReturn(responseDTO);

            WalletResponseDTO result = toggleVisibilityUseCase.execute(USER_ID);

            assertThat(result.getBalance()).isNegative();
        }
    }

    @Nested
    @DisplayName("Manejo de errores")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Debe lanzar excepción cuando el wallet no existe")
        void shouldThrowExceptionWhenWalletNotFound() {
            when(walletService.toggleVisibility(USER_ID))
                .thenThrow(new WalletNotFoundException("Billetera no encontrada"));

            assertThatThrownBy(() -> toggleVisibilityUseCase.execute(USER_ID))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("Billetera no encontrada");

            verify(walletService).toggleVisibility(USER_ID);
        }

        @Test
        @DisplayName("Debe propagar excepciones del servicio sin capturar")
        void shouldPropagateServiceExceptions() {
            when(walletService.toggleVisibility(USER_ID))
                .thenThrow(new InvalidWalletStateException("Estado de wallet inválido"));

            assertThatThrownBy(() -> toggleVisibilityUseCase.execute(USER_ID))
                .isInstanceOf(InvalidWalletStateException.class)
                .hasMessageContaining("Estado de wallet inválido");
        }

        @Test
        @DisplayName("Debe propagar excepciones del balance")
        void shouldPropagateBalanceExceptions() {
            when(walletService.toggleVisibility(USER_ID))
                .thenThrow(new InvalidWalletBalanceException("Balance inválido para toggle"));

            assertThatThrownBy(() -> toggleVisibilityUseCase.execute(USER_ID))
                .isInstanceOf(InvalidWalletBalanceException.class);
        }
    }

    @Nested
    @DisplayName("Comportamiento")
    class BehaviorTests {

        @Test
        @DisplayName("Debe retornar el DTO sin modificaciones")
        void shouldReturnUnmodifiedServiceDTO() {
            WalletResponseDTO expectedDTO = buildWalletResponseDTO(false);
            when(walletService.toggleVisibility(USER_ID)).thenReturn(expectedDTO);

            WalletResponseDTO result = toggleVisibilityUseCase.execute(USER_ID);

            assertThat(result).isEqualTo(expectedDTO);
        }

        @Test
        @DisplayName("Debe manejar múltiples toggles consecutivos")
        void shouldHandleMultipleConsecutiveToggles() {
            WalletResponseDTO dto1 = buildWalletResponseDTO(false);
            WalletResponseDTO dto2 = buildWalletResponseDTO(true);
            when(walletService.toggleVisibility(USER_ID))
                .thenReturn(dto1)
                .thenReturn(dto2);

            WalletResponseDTO result1 = toggleVisibilityUseCase.execute(USER_ID);
            WalletResponseDTO result2 = toggleVisibilityUseCase.execute(USER_ID);

            assertThat(result1.isVisible()).isFalse();
            assertThat(result2.isVisible()).isTrue();
        }
    }

    private WalletResponseDTO buildWalletResponseDTO(boolean visible) {
        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(WALLET_ID);
        dto.setBalance(BALANCE);
        dto.setVisible(visible);
        dto.setUpdatedAt(OffsetDateTime.now());
        return dto;
    }
}
