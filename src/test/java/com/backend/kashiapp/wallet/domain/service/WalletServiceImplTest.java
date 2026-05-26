package com.backend.kashiapp.wallet.domain.service;

import com.backend.kashiapp.common.exception.WalletNotFoundException;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.models.Wallet;
import com.backend.kashiapp.wallet.domain.repository.WalletRepository;
import com.backend.kashiapp.wallet.domain.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("WalletServiceImpl")
class WalletServiceImplTest {

    private WalletRepository walletRepository;
    private WalletServiceImpl walletService;

    private final UUID userId = UUID.randomUUID();
    private final UUID walletId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        walletRepository = mock(WalletRepository.class);
        walletService = new WalletServiceImpl(walletRepository);
    }

    private Wallet buildWallet(BigDecimal balance, boolean visible) {
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setBalance(balance);
        wallet.setVisible(visible);
        return wallet;
    }

    @Nested
    @DisplayName("getWalletByUserId")
    class GetWalletByUserId {

        @Test
        @DisplayName("Debe devolver el DTO con el saldo correcto cuando la billetera existe")
        void shouldReturnWalletDTOWhenFound() {
            Wallet wallet = buildWallet(new BigDecimal("300.00"), true);
            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

            WalletResponseDTO result = walletService.getWalletByUserId(userId);

            assertThat(result.getId()).isEqualTo(walletId);
            assertThat(result.getBalance()).isEqualByComparingTo("300.00");
            assertThat(result.isVisible()).isTrue();
        }

        @Test
        @DisplayName("Debe lanzar WalletNotFoundException cuando no existe billetera para el usuario")
        void shouldThrowWhenWalletNotFound() {
            when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThrows(WalletNotFoundException.class,
                    () -> walletService.getWalletByUserId(userId));
        }
    }

    @Nested
    @DisplayName("toggleVisibility")
    class ToggleVisibility {

        @Test
        @DisplayName("Debe cambiar visible de true a false y persistir el cambio")
        void shouldToggleVisibilityFromTrueToFalse() {
            Wallet wallet = buildWallet(new BigDecimal("100.00"), true);
            Wallet saved = buildWallet(new BigDecimal("100.00"), false);

            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenReturn(saved);

            WalletResponseDTO result = walletService.toggleVisibility(userId);

            assertThat(result.isVisible()).isFalse();
            verify(walletRepository).save(wallet);
        }

        @Test
        @DisplayName("Debe cambiar visible de false a true y persistir el cambio")
        void shouldToggleVisibilityFromFalseToTrue() {
            Wallet wallet = buildWallet(new BigDecimal("100.00"), false);
            Wallet saved = buildWallet(new BigDecimal("100.00"), true);

            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenReturn(saved);

            WalletResponseDTO result = walletService.toggleVisibility(userId);

            assertThat(result.isVisible()).isTrue();
        }

        @Test
        @DisplayName("Debe lanzar WalletNotFoundException cuando no existe billetera para el usuario")
        void shouldThrowWhenWalletNotFound() {
            when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThrows(WalletNotFoundException.class,
                    () -> walletService.toggleVisibility(userId));

            verify(walletRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateBalance")
    class UpdateBalance {

        @Test
        @DisplayName("Debe sumar un monto positivo al saldo existente")
        void shouldAddPositiveAmountToBalance() {
            Wallet wallet = buildWallet(new BigDecimal("200.00"), true);

            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

            walletService.updateBalance(userId, new BigDecimal("50.00"));

            ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
            verify(walletRepository).save(captor.capture());
            assertThat(captor.getValue().getBalance()).isEqualByComparingTo("250.00");
        }

        @Test
        @DisplayName("Debe restar un monto negativo del saldo existente (débito)")
        void shouldSubtractNegativeAmountFromBalance() {
            Wallet wallet = buildWallet(new BigDecimal("200.00"), true);

            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

            walletService.updateBalance(userId, new BigDecimal("-75.00"));

            ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
            verify(walletRepository).save(captor.capture());
            assertThat(captor.getValue().getBalance()).isEqualByComparingTo("125.00");
        }

        @Test
        @DisplayName("Debe lanzar WalletNotFoundException cuando no existe billetera para el usuario")
        void shouldThrowWhenWalletNotFound() {
            when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThrows(WalletNotFoundException.class,
                    () -> walletService.updateBalance(userId, new BigDecimal("100.00")));

            verify(walletRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe persistir la billetera después de actualizar el saldo")
        void shouldSaveWalletAfterBalanceUpdate() {
            Wallet wallet = buildWallet(new BigDecimal("100.00"), true);

            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

            walletService.updateBalance(userId, new BigDecimal("10.00"));

            verify(walletRepository, times(1)).save(any(Wallet.class));
        }
    }
}
