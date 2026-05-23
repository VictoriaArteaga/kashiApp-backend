package com.backend.kashiapp.wallet.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.kashiapp.common.exception.WalletNotFoundException;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.models.Wallet;
import com.backend.kashiapp.wallet.domain.repository.WalletRepository;
import com.backend.kashiapp.wallet.domain.service.impl.WalletServiceImpl;

// wallet service

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del servicio WalletService")
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    private WalletService walletService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

    @BeforeEach
    void setup() {
        walletService = new WalletServiceImpl(walletRepository);
    }

    @Nested
    @DisplayName("getWalletByUserId")
    class GetWalletByUserIdTests {

        @Test
        @DisplayName("Debe retornar el DTO cuando el wallet existe")
        void shouldReturnWalletResponseDTOWhenWalletExists() {
            Wallet wallet = buildWallet();
            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));

            WalletResponseDTO result = walletService.getWalletByUserId(USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(WALLET_ID);
            assertThat(result.getBalance()).isEqualTo(INITIAL_BALANCE);
            assertThat(result.isVisible()).isTrue();
            verify(walletRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el wallet no existe")
        void shouldThrowWalletNotFoundExceptionWhenWalletDoesNotExist() {
            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.getWalletByUserId(USER_ID))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("No se encontró la billetera");

            verify(walletRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("Debe retornar balance correcto")
        void shouldReturnCorrectBalance() {
            Wallet wallet = buildWallet();
            wallet.setBalance(new BigDecimal("5000.50"));
            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));

            WalletResponseDTO result = walletService.getWalletByUserId(USER_ID);

            assertThat(result.getBalance()).isEqualTo(new BigDecimal("5000.50"));
        }
    }

    @Nested
    @DisplayName("toggleVisibility")
    class ToggleVisibilityTests {

        @Test
        @DisplayName("Debe cambiar la visibilidad y guardar")
        void shouldToggleVisibilityAndSave() {
            Wallet wallet = buildWallet();
            wallet.setVisible(true);
            Wallet updatedWallet = buildWallet();
            updatedWallet.setVisible(false);

            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenReturn(updatedWallet);

            WalletResponseDTO result = walletService.toggleVisibility(USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.isVisible()).isFalse();
            verify(walletRepository).findByUserId(USER_ID);
            verify(walletRepository).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el wallet no existe")
        void shouldThrowExceptionWhenWalletNotFound() {
            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.toggleVisibility(USER_ID))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("Billetera no encontrada");

            verify(walletRepository, never()).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Debe actualizar el timestamp")
        void shouldUpdateTimestamp() {
            Wallet wallet = buildWallet();
            Wallet updatedWallet = buildWallet();
            updatedWallet.setUpdatedAt(OffsetDateTime.now());

            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenReturn(updatedWallet);

            walletService.toggleVisibility(USER_ID);

            verify(walletRepository).save(any(Wallet.class));
        }
    }

    @Nested
    @DisplayName("updateBalance")
    class UpdateBalanceTests {

        @Test
        @DisplayName("Debe actualizar el saldo con un depósito")
        void shouldUpdateBalanceWithDeposit() {
            Wallet wallet = buildWallet();
            BigDecimal depositAmount = new BigDecimal("500.00");

            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

            walletService.updateBalance(USER_ID, depositAmount);

            verify(walletRepository).findByUserId(USER_ID);
            verify(walletRepository).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Debe actualizar el saldo con un retiro")
        void shouldUpdateBalanceWithWithdrawal() {
            Wallet wallet = buildWallet();
            BigDecimal withdrawAmount = new BigDecimal("-300.00");

            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

            walletService.updateBalance(USER_ID, withdrawAmount);

            verify(walletRepository).findByUserId(USER_ID);
            verify(walletRepository).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el wallet no existe")
        void shouldThrowExceptionWhenWalletNotFound() {
            BigDecimal amount = new BigDecimal("100.00");
            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.updateBalance(USER_ID, amount))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("No se encontró la billetera para actualizar el saldo");

            verify(walletRepository, never()).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Debe permitir saldo negativo")
        void shouldAllowNegativeBalance() {
            Wallet wallet = buildWallet();
            BigDecimal largeWithdrawal = new BigDecimal("-2000.00");

            when(walletRepository.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

            walletService.updateBalance(USER_ID, largeWithdrawal);

            verify(walletRepository).save(any(Wallet.class));
        }
    }

    private Wallet buildWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(WALLET_ID);
        wallet.setUserId(USER_ID);
        wallet.setBalance(INITIAL_BALANCE);
        wallet.setVisible(true);
        wallet.setUpdatedAt(OffsetDateTime.now());
        return wallet;
    }
}
