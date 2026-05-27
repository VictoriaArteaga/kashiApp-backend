package com.backend.kashiapp.integration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.kashiapp.TestcontainersConfiguration;
import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.JpaUserRepository;
import com.backend.kashiapp.user.infraestructure.security.JwtService;
import com.backend.kashiapp.wallet.infraestructure.persistence.JpaWalletRepository;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JpaWalletRepository walletRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    private User sender;
    private User receiver;
    private String senderToken;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        jpaUserRepository.deleteAll();

        // 1. Crear Emisor
        sender = new User();
        sender.setEmail("sender@test.com");
        sender.setPasswordHash("hash123");
        sender.setUsername("sender_user");
        sender.setNumberPhone("1234567890");
        sender.setAccountStatus(AccountStatus.ACTIVE);
        sender.setIdentificationNumber("ID-SENDER");
        sender.setCreationDate(OffsetDateTime.now());
        sender = userRepository.save(sender);

        WalletEntity senderWallet = new WalletEntity();
        senderWallet.setUserId(sender.getId());
        senderWallet.setBalance(new BigDecimal("1000.00"));
        senderWallet.setVisible(true);
        walletRepository.save(senderWallet);

        // 2. Crear Receptor
        receiver = new User();
        receiver.setEmail("receiver@test.com");
        receiver.setPasswordHash("hash123");
        receiver.setUsername("receiver_user");
        receiver.setNumberPhone("0987654321");
        receiver.setAccountStatus(AccountStatus.ACTIVE);
        receiver.setIdentificationNumber("ID-RECEIVER");
        receiver.setCreationDate(OffsetDateTime.now());
        receiver = userRepository.save(receiver);

        WalletEntity receiverWallet = new WalletEntity();
        receiverWallet.setUserId(receiver.getId());
        receiverWallet.setBalance(new BigDecimal("500.00"));
        receiverWallet.setVisible(true);
        walletRepository.save(receiverWallet);

        // 3. Generar JWT
        senderToken = jwtService.generateToken(sender.getEmail());
    }

    @Test
    @DisplayName("KAN-70: Consultar el saldo y compararlo con el de base de datos")
    void shouldVerifyBalanceMatchesDatabase() throws Exception {
        // En base de datos el emisor tiene 1000.00
        mockMvc.perform(get("/api/v1/wallets/balance")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Asumiendo que WalletResponseDTO tiene un campo "balance" y devuelve ApiResponse
                .andExpect(jsonPath("$.data.balance").value(1000.00));
    }

    @Test
    @DisplayName("KAN-268 y KAN-276: Transferencia e inmediatamente verificar saldo (Sincronizado a Supabase/PostgreSQL)")
    void shouldVerifyBalanceImmediatelyAfterTransfer() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(new BigDecimal("200.00"));

        // Hacemos el POST de transferencia
        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verificamos inmediatamente usando la base de datos (KAN-276)
        WalletEntity updatedSenderWallet = walletRepository.findByUserId(sender.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("800.00").compareTo(updatedSenderWallet.getBalance()), "El saldo del emisor debe descontarse en la DB");

        WalletEntity updatedReceiverWallet = walletRepository.findByUserId(receiver.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("700.00").compareTo(updatedReceiverWallet.getBalance()), "El saldo del receptor debe aumentar en la DB");

        // Y verificamos usando la API (KAN-268)
        mockMvc.perform(get("/api/v1/wallets/balance")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(800.00));
    }

    @Test
    @DisplayName("KAN-68: Modificar DTO durante ejecución simulando tampering es rechazado")
    void shouldRejectTransactionTampering() throws Exception {
        // Si alguien intercepta la petición y cambia el monto a negativo o a un destinatario inválido,
        // el DTO validation o la lógica deben fallar.
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(new BigDecimal("-50.00")); // Monto inválido

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // O el código que tu GlobalExceptionHandler devuelva para InvalidTransactionAmount
    }
}
