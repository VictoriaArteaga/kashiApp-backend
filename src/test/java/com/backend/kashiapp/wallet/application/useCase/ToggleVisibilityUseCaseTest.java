package com.backend.kashiapp.wallet.application.useCase;

import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("ToggleVisibilityUseCase")
class ToggleVisibilityUseCaseTest {

    private WalletService walletService;
    private ToggleVisibilityUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        walletService = mock(WalletService.class);
        useCase = new ToggleVisibilityUseCase(walletService);
    }

    @Test
    @DisplayName("Debe devolver la respuesta del servicio sin transformarla")
    void shouldReturnServiceResponseDirectly() {
        WalletResponseDTO expected = new WalletResponseDTO();
        expected.setId(UUID.randomUUID());
        expected.setVisible(false);

        when(walletService.toggleVisibility(userId)).thenReturn(expected);

        WalletResponseDTO result = useCase.execute(userId);

        assertThat(result).isSameAs(expected);
        verify(walletService).toggleVisibility(userId);
    }

    @Test
    @DisplayName("Debe delegar la llamada al servicio con el userId correcto")
    void shouldDelegateToServiceWithCorrectUserId() {
        when(walletService.toggleVisibility(userId)).thenReturn(new WalletResponseDTO());

        useCase.execute(userId);

        verify(walletService).toggleVisibility(userId);
    }
}
