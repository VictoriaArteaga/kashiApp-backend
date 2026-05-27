package com.backend.kashiapp.wallet.application.useCase;

import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("GetBalanceUseCase")
class GetBalanceUseCaseTest {

    private WalletService walletService;
    private GetBalanceUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        walletService = mock(WalletService.class);
        useCase = new GetBalanceUseCase(walletService);
    }

    @Test
    @DisplayName("Debe devolver la respuesta del servicio sin transformarla")
    void shouldReturnServiceResponseDirectly() {
        WalletResponseDTO expected = new WalletResponseDTO();
        expected.setId(UUID.randomUUID());
        expected.setBalance(new BigDecimal("500.00"));
        expected.setVisible(true);

        when(walletService.getWalletByUserId(userId)).thenReturn(expected);

        WalletResponseDTO result = useCase.execute(userId);

        assertThat(result).isSameAs(expected);
        verify(walletService).getWalletByUserId(userId);
    }

    @Test
    @DisplayName("Debe delegar la llamada al servicio con el userId correcto")
    void shouldDelegateToServiceWithCorrectUserId() {
        when(walletService.getWalletByUserId(userId)).thenReturn(new WalletResponseDTO());

        useCase.execute(userId);

        verify(walletService).getWalletByUserId(userId);
    }
}
