package com.backend.kashiapp.transaction.domain.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;

import com.backend.kashiapp.common.exception.InsufficientFundsException;
import com.backend.kashiapp.common.exception.InvalidRecipientStateException;
import com.backend.kashiapp.common.exception.RecipientNotFoundException;
import com.backend.kashiapp.common.exception.SelfTransferNotAllowedException;
import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;
import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.TransferCompletedEvent;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import com.backend.kashiapp.transaction.domain.repository.TransactionRepository;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.service.WalletService;

@DisplayName("TransferStrategy")
class TransferStrategyTest {

    private WalletService walletService;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private ApplicationEventPublisher eventPublisher;
    private TransferStrategy transferStrategy;

    private final UUID senderId = UUID.randomUUID();
    private final UUID recipientId = UUID.randomUUID();
    private final UUID senderWalletId = UUID.randomUUID();
    private final UUID recipientWalletId = UUID.randomUUID();
    private final String recipientEmail = "recipient@example.com";
    private final BigDecimal amount = new BigDecimal("100.00");

    @BeforeEach
    void setup() {
        walletService = mock(WalletService.class);
        userRepository = mock(UserRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        transferStrategy = new TransferStrategy(
                walletService,
                userRepository,
                transactionRepository,
                eventPublisher
        );
    }

    // Construye un destinatario válido con el estado de cuenta indicado.
    private UserEntity buildRecipient(AccountStatus status) {
        UserEntity recipient = new UserEntity();
        recipient.setId(recipientId);
        recipient.setEmail(recipientEmail);
        recipient.setAccountStatus(status);
        return recipient;
    }

    // Construye una respuesta de billetera con el id y saldo indicados.
    private WalletResponseDTO buildWallet(UUID id, BigDecimal balance) {
        WalletResponseDTO wallet = new WalletResponseDTO();
        wallet.setId(id);
        wallet.setBalance(balance);
        return wallet;
    }

    // Construye el DTO de petición usado en todos los tests.
    private TransactionRequestDTO buildRequest() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(recipientEmail);
        request.setAmount(amount);
        return request;
    }

    // Prepara un escenario válido: destinatario ACTIVE y billeteras presentes en el wallet service.
    private void stubValidScenario(BigDecimal senderBalance) {
        UserEntity recipient = buildRecipient(AccountStatus.ACTIVE);
        WalletResponseDTO senderWallet = buildWallet(senderWalletId, senderBalance);
        WalletResponseDTO recipientWallet = buildWallet(recipientWalletId, BigDecimal.ZERO);

        when(userRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));
        when(walletService.getWalletByUserId(senderId)).thenReturn(senderWallet);
        when(walletService.getWalletByUserId(recipientId)).thenReturn(recipientWallet);
    }

    @Test
    @DisplayName("Debe identificarse con el tipo TRANSFER para que la fábrica la encuentre")
    void shouldReturnTypeTransfer() {
        assertThat(transferStrategy.getType()).isEqualTo("TRANSFER");
        assertThat(transferStrategy.getType()).isEqualTo(TransferStrategy.TYPE);
    }

    @Nested
    @DisplayName("Successful execution")
    class SuccessfulExecution {

        @Test
        @DisplayName("Debe completar la transferencia y devolver estado COMPLETED")
        void shouldExecuteTransferSuccessfully() {
            stubValidScenario(new BigDecimal("500.00"));

            TransactionResponseDTO response = transferStrategy.execute(senderId, buildRequest());

            // La respuesta debe contener una referencia generada, el mismo monto y estado COMPLETED.
            assertThat(response.getTransactionReference()).isNotNull();
            assertThat(response.getAmount()).isEqualByComparingTo(amount);
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        }

        @Test
        @DisplayName("Debe debitar al emisor y acreditar al receptor")
        void shouldDebitSenderAndCreditRecipient() {
            stubValidScenario(new BigDecimal("500.00"));

            transferStrategy.execute(senderId, buildRequest());

            // Al emisor se le debita -amount y al receptor se le acredita +amount.
            verify(walletService).updateBalance(senderId, amount.negate());
            verify(walletService).updateBalance(recipientId, amount);
        }

        @Test
        @DisplayName("Debe permitir transferir cuando el saldo es exactamente igual al monto")
        void shouldAllowTransferWhenBalanceEqualsAmount() {
            // Caso borde: compareTo == 0 debe aceptarse.
            stubValidScenario(amount);

            TransactionResponseDTO response = transferStrategy.execute(senderId, buildRequest());

            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        }

        @Test
        @DisplayName("Debe preservar la precisión de BigDecimal con decimales")
        void shouldHandleAmountsWithMultipleDecimals() {
            // La precisión de BigDecimal debe llegar tal cual a la respuesta y al wallet service.
            BigDecimal preciseAmount = new BigDecimal("123.45");
            stubValidScenario(new BigDecimal("1000.00"));

            TransactionRequestDTO request = new TransactionRequestDTO();
            request.setRecipientEmail(recipientEmail);
            request.setAmount(preciseAmount);

            TransactionResponseDTO response = transferStrategy.execute(senderId, request);

            assertThat(response.getAmount()).isEqualByComparingTo(preciseAmount);
            verify(walletService).updateBalance(eq(senderId), eq(preciseAmount.negate()));
            verify(walletService).updateBalance(eq(recipientId), eq(preciseAmount));
        }
    }

    @Nested
    @DisplayName("Efectos secundarios y persistencia")
    class SideEffects {

        @Test
        @DisplayName("Debe persistir OUTGOING e INCOMING con la misma referencia")
        void shouldPersistTwoTransactionsWithSameReference() {
            stubValidScenario(new BigDecimal("500.00"));

            transferStrategy.execute(senderId, buildRequest());

            // Capturamos las dos transacciones persistidas y validamos referencia compartida y dirección.
            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository, times(2)).save(captor.capture());

            Transaction outgoing = captor.getAllValues().get(0);
            Transaction incoming = captor.getAllValues().get(1);

            // La misma referencia en ambas filas permite rastrearlas como una sola transferencia.
            assertThat(outgoing.getTransferReference()).isEqualTo(incoming.getTransferReference());
            assertThat(outgoing.getType()).isEqualTo(TransactionType.OUTGOING);
            assertThat(incoming.getType()).isEqualTo(TransactionType.INCOMING);
            assertThat(outgoing.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(incoming.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(outgoing.getSenderWalletId()).isEqualTo(senderWalletId);
            assertThat(outgoing.getReceiverWalletId()).isEqualTo(recipientWalletId);
        }

        @Test
        @DisplayName("Debe publicar TransferCompletedEvent con todos los datos para el listener")
        void shouldPublishTransferCompletedEvent() {
            stubValidScenario(new BigDecimal("500.00"));

            transferStrategy.execute(senderId, buildRequest());

            // Capturamos el evento y verificamos que lleve todos los campos que necesita el listener.
            ArgumentCaptor<TransferCompletedEvent> captor =
                    ArgumentCaptor.forClass(TransferCompletedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            TransferCompletedEvent event = captor.getValue();
            assertThat(event.getSenderUserId()).isEqualTo(senderId);
            assertThat(event.getRecipientUserId()).isEqualTo(recipientId);
            assertThat(event.getAmount()).isEqualByComparingTo(amount);
            assertThat(event.getTransferReference()).isNotNull();
        }

        @Test
        @DisplayName("Debe ejecutar validar -> debitar -> acreditar -> guardar -> publicar en orden")
        void shouldExecuteOperationsInCorrectOrder() {
            // Este test protege contra refactors que rompan la invariante transaccional.
            stubValidScenario(new BigDecimal("500.00"));

            transferStrategy.execute(senderId, buildRequest());

            InOrder inOrder = inOrder(walletService, transactionRepository, eventPublisher);
            inOrder.verify(walletService).updateBalance(senderId, amount.negate());
            inOrder.verify(walletService).updateBalance(recipientId, amount);
            inOrder.verify(transactionRepository, times(2)).save(any(Transaction.class));
            inOrder.verify(eventPublisher).publishEvent(any(TransferCompletedEvent.class));
        }
    }

    @Nested
    @DisplayName("Validación del destinatario")
    class RecipientValidation {

        @Test
        @DisplayName("Debe lanzar RecipientNotFoundException si el email no existe")
        void shouldThrowRecipientNotFoundExceptionWhenEmailDoesNotExist() {

            when(userRepository.findByEmail(recipientEmail)).thenReturn(Optional.empty());

            RecipientNotFoundException ex = assertThrows(
                    RecipientNotFoundException.class,
                    () -> transferStrategy.execute(senderId, buildRequest())
            );
            assertThat(ex.getMessage()).isEqualTo("Destinatario no encontrado");

            // No debe moverse ninguna billetera ni guardarse ninguna transacción.
            verify(walletService, never()).updateBalance(any(), any());
            verify(transactionRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Debe lanzar SelfTransferNotAllowedException si el emisor es el receptor")
        void shouldThrowSelfTransferNotAllowedExceptionWhenSenderEqualsRecipient() {

            // Un usuario que intenta enviarse dinero a sí mismo debe ser rechazado antes de tocar el wallet.
            UserEntity recipient = buildRecipient(AccountStatus.ACTIVE);
            recipient.setId(senderId);

            when(userRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

            SelfTransferNotAllowedException ex = assertThrows(
                    SelfTransferNotAllowedException.class,
                    () -> transferStrategy.execute(senderId, buildRequest())
            );
            assertThat(ex.getMessage()).isEqualTo("No puedes transferirte dinero a ti mismo");

            // La estrategia debe fallar rápido: no debe consultar billeteras ni actualizar saldos.
            verify(walletService, never()).getWalletByUserId(any());
            verify(walletService, never()).updateBalance(any(), any());
            verify(transactionRepository, never()).save(any());
        }

        @ParameterizedTest(name = "Estado {0} del destinatario debe ser rechazado")
        @EnumSource(value = AccountStatus.class, names = {"INACTIVE", "SUSPENDED", "DELETED", "BLOCKED"})
        @DisplayName("Debe lanzar InvalidRecipientStateException para cualquier estado distinto a ACTIVE")
        void shouldThrowInvalidRecipientStateExceptionForNonActiveAccounts(AccountStatus status) {

            // Solo las cuentas ACTIVE pueden recibir dinero: cualquier otro estado se rechaza.
            UserEntity recipient = buildRecipient(status);
            when(userRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));

            InvalidRecipientStateException ex = assertThrows(
                    InvalidRecipientStateException.class,
                    () -> transferStrategy.execute(senderId, buildRequest())
            );
            assertThat(ex.getMessage()).isEqualTo("La cuenta del destinatario no está disponible");

            verify(walletService, never()).updateBalance(any(), any());
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Validación de fondos")
    class FundsValidation {

        @Test
        @DisplayName("Debe lanzar InsufficientFundsException si el saldo es menor al monto")
        void shouldThrowInsufficientFundsExceptionWhenBalanceIsLowerThanAmount() {
            // Un saldo inferior al monto debe abortar la transferencia antes de tocar ninguna billetera.
            UserEntity recipient = buildRecipient(AccountStatus.ACTIVE);
            WalletResponseDTO senderWallet = buildWallet(senderWalletId, new BigDecimal("10.00"));
            WalletResponseDTO recipientWallet = buildWallet(recipientWalletId, BigDecimal.ZERO);

            when(userRepository.findByEmail(recipientEmail)).thenReturn(Optional.of(recipient));
            when(walletService.getWalletByUserId(senderId)).thenReturn(senderWallet);
            when(walletService.getWalletByUserId(recipientId)).thenReturn(recipientWallet);

            InsufficientFundsException ex = assertThrows(
                    InsufficientFundsException.class,
                    () -> transferStrategy.execute(senderId, buildRequest())
            );
            assertThat(ex.getMessage()).isEqualTo("Fondos insuficientes");

            // No debe moverse dinero ni persistirse ninguna transacción.
            verify(walletService, never()).updateBalance(any(), any());
            verify(transactionRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}
