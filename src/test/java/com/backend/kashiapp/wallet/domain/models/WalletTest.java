package com.backend.kashiapp.wallet.domain.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

// modelo wallet

@DisplayName("Pruebas del modelo Wallet")
class WalletTest {

    private Wallet wallet;
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");

    @BeforeEach
    void setup() {
        wallet = new Wallet();
        wallet.setId(WALLET_ID);
        wallet.setUserId(USER_ID);
        wallet.setBalance(INITIAL_BALANCE);
        wallet.setVisible(true);
        wallet.setUpdatedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("applyBalanceChange")
    class ApplyBalanceChangeTests {

        @Test
        @DisplayName("Debe aumentar el saldo con depósito")
        void shouldDepositMoneyAndIncreaseBalance() {
            BigDecimal depositAmount = new BigDecimal("100.00");
            BigDecimal expectedBalance = INITIAL_BALANCE.add(depositAmount);

            wallet.applyBalanceChange(depositAmount);

            assertThat(wallet.getBalance()).isEqualTo(expectedBalance);
        }

        @Test
        @DisplayName("Debe disminuir el saldo con retiro")
        void shouldWithdrawMoneyAndDecreaseBalance() {
            BigDecimal withdrawAmount = new BigDecimal("-150.00");
            BigDecimal expectedBalance = INITIAL_BALANCE.add(withdrawAmount);

            wallet.applyBalanceChange(withdrawAmount);

            assertThat(wallet.getBalance()).isEqualTo(expectedBalance);
        }

        @Test
        @DisplayName("Debe actualizar el timestamp")
        void shouldUpdateTimestampWhenApplyingBalanceChange() {
            OffsetDateTime beforeUpdate = wallet.getUpdatedAt();

            wallet.applyBalanceChange(new BigDecimal("50.00"));

            assertThat(wallet.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        @DisplayName("Debe permitir saldo negativo")
        void shouldAllowNegativeBalance() {
            BigDecimal largeWithdrawal = new BigDecimal("-600.00");
            BigDecimal expectedBalance = INITIAL_BALANCE.add(largeWithdrawal);

            wallet.applyBalanceChange(largeWithdrawal);

            assertThat(wallet.getBalance()).isEqualTo(expectedBalance);
            assertThat(wallet.getBalance()).isNegative();
        }

        @Test
        @DisplayName("Debe manejar múltiples cambios consecutivos")
        void shouldHandleMultipleConsecutiveBalanceChanges() {
            BigDecimal deposit1 = new BigDecimal("100.00");
            BigDecimal deposit2 = new BigDecimal("50.00");
            BigDecimal withdrawal = new BigDecimal("-30.00");
            BigDecimal expectedBalance = INITIAL_BALANCE.add(deposit1).add(deposit2).add(withdrawal);

            wallet.applyBalanceChange(deposit1);
            wallet.applyBalanceChange(deposit2);
            wallet.applyBalanceChange(withdrawal);

            assertThat(wallet.getBalance()).isEqualTo(expectedBalance);
        }

        @Test
        @DisplayName("Debe manejar monto cero")
        void shouldHandleZeroAmount() {
            wallet.applyBalanceChange(BigDecimal.ZERO);

            assertThat(wallet.getBalance()).isEqualTo(INITIAL_BALANCE);
        }
    }

    @Nested
    @DisplayName("toggleVisibility")
    class ToggleVisibilityTests {

        @Test
        @DisplayName("Debe cambiar de visible a invisible")
        void shouldToggleFromVisibleToInvisible() {
            wallet.setVisible(true);

            wallet.toggleVisibility();

            assertThat(wallet.isVisible()).isFalse();
        }

        @Test
        @DisplayName("Debe cambiar de invisible a visible")
        void shouldToggleFromInvisibleToVisible() {
            wallet.setVisible(false);

            wallet.toggleVisibility();

            assertThat(wallet.isVisible()).isTrue();
        }

        @Test
        @DisplayName("Debe alternar múltiples veces")
        void shouldToggleVisibilityMultipleTimes() {
            boolean initialVisibility = wallet.isVisible();

            wallet.toggleVisibility();
            wallet.toggleVisibility();
            wallet.toggleVisibility();

            assertThat(wallet.isVisible()).isNotEqualTo(initialVisibility);
        }

        @Test
        @DisplayName("Debe actualizar el timestamp")
        void shouldUpdateTimestampWhenTogglingVisibility() {
            OffsetDateTime beforeToggle = wallet.getUpdatedAt();

            wallet.toggleVisibility();

            assertThat(wallet.getUpdatedAt()).isAfterOrEqualTo(beforeToggle);
        }
    }

    @Nested
    @DisplayName("Integración de operaciones")
    class IntegrationTests {

        @Test
        @DisplayName("Debe aplicar cambio de saldo y cambiar visibilidad")
        void shouldApplyBalanceChangeAndToggleVisibility() {
            BigDecimal depositAmount = new BigDecimal("200.00");
            BigDecimal expectedBalance = INITIAL_BALANCE.add(depositAmount);

            wallet.applyBalanceChange(depositAmount);
            wallet.toggleVisibility();

            assertThat(wallet.getBalance()).isEqualTo(expectedBalance);
            assertThat(wallet.isVisible()).isFalse();
        }

        @Test
        @DisplayName("Debe mantener integridad después de múltiples operaciones")
        void shouldMaintainCorrectAttributesAfterMultipleOperations() {
            UUID originalId = wallet.getId();
            UUID originalUserId = wallet.getUserId();

            wallet.applyBalanceChange(new BigDecimal("100.00"));
            wallet.toggleVisibility();
            wallet.applyBalanceChange(new BigDecimal("-50.00"));
            wallet.toggleVisibility();

            assertThat(wallet.getId()).isEqualTo(originalId);
            assertThat(wallet.getUserId()).isEqualTo(originalUserId);
            assertThat(wallet.getBalance()).isEqualTo(new BigDecimal("550.00"));
            assertThat(wallet.isVisible()).isTrue();
        }
    }
}
