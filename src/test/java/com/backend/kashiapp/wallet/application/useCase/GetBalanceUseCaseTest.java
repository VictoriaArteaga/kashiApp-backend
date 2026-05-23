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
import com.backend.kashiapp.common.exception.WalletNotFoundException;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.service.WalletService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del caso de uso GetBalanceUseCase")
class GetBalanceUseCaseTest {

    @Mock
    private WalletService walletService;

    private GetBalanceUseCase getBalanceUseCase;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final BigDecimal BALANCE = new BigDecimal("1500.50");

    @BeforeEach
    void setup() {
        getBalanceUseCase = new GetBalanceUseCase(walletService);
    }

    @Nested
    @DisplayName("Obtención exitosa del saldo")
    class SuccessfulRetrievalTests {

        @Test
        @DisplayName("Debe retornar el DTO de respuesta con el saldo correcto")
        void shouldReturnResponseDTOWithCorrectBalance() {
            WalletResponseDTO expectedDTO = buildWalletResponseDTO();
            when(walletService.getWalletByUserId(USER_ID)).thenReturn(expectedDTO);

            WalletResponseDTO result = getBalanceUseCase.execute(USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(WALLET_ID);
            assertThat(result.getBalance()).isEqualTo(BALANCE);
            verify(walletService).getWalletByUserId(USER_ID);
        }

        @Test
        @DisplayName("Debe retornar balance negativo cuando existe deuda")
        void shouldReturnNegativeBalanceWhenInDebt() {
            WalletResponseDTO expectedDTO = buildWalletResponseDTO();
            expectedDTO.setBalance(new BigDecimal("-500.00"));
            when(walletService.getWalletByUserId(USER_ID)).thenReturn(expectedDTO);

            WalletResponseDTO result = getBalanceUseCase.execute(USER_ID);

            assertThat(result.getBalance()).isNegative();
            assertThat(result.getBalance()).isEqualTo(new BigDecimal("-500.00"));
        }
    }

    @Nested
    @DisplayName("Manejo de errores")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Debe lanzar excepción cuando el wallet no existe")
        void shouldThrowExceptionWhenWalletNotFound() {
            when(walletService.getWalletByUserId(USER_ID))
                .thenThrow(new WalletNotFoundException("Wallet no encontrado"));

            assertThatThrownBy(() -> getBalanceUseCase.execute(USER_ID))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("Wallet no encontrado");

            verify(walletService).getWalletByUserId(USER_ID);
        }

        @Test
        @DisplayName("Debe propagar excepciones del servicio")
        void shouldPropagateServiceException() {
            when(walletService.getWalletByUserId(USER_ID))
                .thenThrow(new InvalidWalletBalanceException("Balance inválido"));

            assertThatThrownBy(() -> getBalanceUseCase.execute(USER_ID))
                .isInstanceOf(InvalidWalletBalanceException.class)
                .hasMessageContaining("Balance inválido");
        }
    }

    @Nested
    @DisplayName("Comportamiento")
    class BehaviorTests {

        @Test
        @DisplayName("Debe retornar el DTO sin modificaciones")
        void shouldReturnUnmodifiedServiceDTO() {
            WalletResponseDTO expectedDTO = buildWalletResponseDTO();
            when(walletService.getWalletByUserId(USER_ID)).thenReturn(expectedDTO);

            WalletResponseDTO result = getBalanceUseCase.execute(USER_ID);

            assertThat(result).isNotNull().isEqualTo(expectedDTO);
        }
    }

    private WalletResponseDTO buildWalletResponseDTO() {
        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(WALLET_ID);
        dto.setBalance(BALANCE);
        dto.setVisible(true);
        dto.setUpdatedAt(OffsetDateTime.now());
        return dto;
    }
}
