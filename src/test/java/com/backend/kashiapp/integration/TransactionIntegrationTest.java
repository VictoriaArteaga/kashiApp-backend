package com.backend.kashiapp.integration;

import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransactionIntegrationTest extends BaseIntegrationTest {

    private UserEntity sender;
    private UserEntity receiver;
    private String senderToken;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        // Emisor con saldo 1000 y receptor con saldo 500.
        sender = persistActiveUser("sender@test.com", "sender_user", "1234567890", "ID-SENDER");
        persistWallet(sender.getId(), new BigDecimal("1000.00"));

        receiver = persistActiveUser("receiver@test.com", "receiver_user", "0987654321", "ID-RECEIVER");
        persistWallet(receiver.getId(), new BigDecimal("500.00"));

        senderToken = jwtService.generateToken(sender.getEmail());
    }

    @Test
    @DisplayName("KAN-70: Consultar el saldo y compararlo con el de base de datos")
    void shouldVerifyBalanceMatchesDatabase() throws Exception {
        mockMvc.perform(get("/api/v1/wallet/balance")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(1000.00));
    }

    @Test
    @DisplayName("KAN-268 y KAN-276: Transferencia e inmediatamente verificar saldo")
    void shouldVerifyBalanceImmediatelyAfterTransfer() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(new BigDecimal("200.00"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        WalletEntity updatedSenderWallet = walletRepository.findByUserId(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("800.00").compareTo(updatedSenderWallet.getBalance()),
                "El saldo del emisor debe descontarse en la DB");

        WalletEntity updatedReceiverWallet = walletRepository.findByUserId(receiver.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("700.00").compareTo(updatedReceiverWallet.getBalance()),
                "El saldo del receptor debe aumentar en la DB");

        mockMvc.perform(get("/api/v1/wallet/balance")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(800.00));
    }

    // ======================================================================
    // KAN-68: Tampering — modificar monto o destinatario durante la ejecución
    // ======================================================================

    @Test
    @DisplayName("KAN-68a: Tampering — monto negativo es rechazado por validación @Positive")
    void shouldRejectTampering_negativeAmount() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(new BigDecimal("-50.00"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // El saldo del emisor no debe cambiar tras un intento de tampering
        WalletEntity finalSender = walletRepository.findByUserId(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalSender.getBalance()),
                "El saldo no debe modificarse cuando el tampering es rechazado");
    }

    @Test
    @DisplayName("KAN-68b: Tampering — monto cero es rechazado por validación @Positive")
    void shouldRejectTampering_zeroAmount() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        WalletEntity finalSender = walletRepository.findByUserId(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalSender.getBalance()),
                "El saldo no debe modificarse cuando el monto es cero");
    }

    @Test
    @DisplayName("KAN-68c: Tampering — destinatario con email mal formado es rechazado por @Email")
    void shouldRejectTampering_invalidRecipientEmail() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail("no-es-email");
        request.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        WalletEntity finalSender = walletRepository.findByUserId(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalSender.getBalance()),
                "El saldo no debe modificarse con email inválido");
    }

    @Test
    @DisplayName("KAN-68d: Tampering — destinatario inexistente es rechazado (404)")
    void shouldRejectTampering_nonexistentRecipient() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail("no_existe@test.com");
        request.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        WalletEntity finalSender = walletRepository.findByUserId(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalSender.getBalance()),
                "El saldo no debe modificarse al transferir a un destinatario inexistente");
    }

    @Test
    @DisplayName("KAN-68e: Tampering — auto-transferencia rechazada (sender == receiver)")
    void shouldRejectTampering_selfTransfer() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(sender.getEmail());
        request.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        WalletEntity finalSender = walletRepository.findByUserId(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalSender.getBalance()),
                "El saldo no debe cambiar en una auto-transferencia rechazada");
    }

    @Test
    @DisplayName("KAN-68f: Tampering — monto superior al saldo es rechazado (fondos insuficientes)")
    void shouldRejectTampering_amountExceedsBalance() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(new BigDecimal("99999.00"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        WalletEntity finalSender = walletRepository.findByUserId(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalSender.getBalance()),
                "El saldo del emisor debe permanecer intacto");

        WalletEntity finalReceiver = walletRepository.findByUserId(receiver.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("500.00").compareTo(finalReceiver.getBalance()),
                "El saldo del receptor no debe acreditarse si la operación falló");
    }
}
