package com.backend.kashiapp.integration;

import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.domain.repository.TransactionRepository;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.wallet.infraestructure.persistence.WalletEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Verifica la atomicidad de la transferencia: si un paso posterior al débito falla,
 * la transacción @Transactional debe revertir TODO y dejar los saldos intactos.
 */
@DisplayName("Atomicidad de la transferencia: rollback ante fallo intermedio")
public class TransactionRollbackIntegrationTest extends BaseIntegrationTest {

    // Espiamos el repositorio real (de dominio) para forzar un fallo justo al persistir la
    // transacción, que ocurre DESPUÉS de debitar al emisor y acreditar al receptor.
    // Nombre distinto al de la base para no sombrear su JpaTransactionRepository.
    @SpyBean
    private TransactionRepository domainTransactionRepository;

    private User sender;
    private User receiver;
    private String senderToken;

    private static final BigDecimal SENDER_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal RECEIVER_BALANCE = new BigDecimal("500.00");

    @BeforeEach
    void setUp() {
        cleanDatabase();

        sender = persistActiveUser("rb_sender@test.com", "rb_sender", "5151515151", "ID-RB-SENDER");
        persistWallet(sender.getId(), SENDER_BALANCE);

        receiver = persistActiveUser("rb_receiver@test.com", "rb_receiver", "6262626262", "ID-RB-RECEIVER");
        persistWallet(receiver.getId(), RECEIVER_BALANCE);

        senderToken = jwtService.generateToken(sender.getEmail());
    }

    @Test
    @DisplayName("Si falla la persistencia de la transacción, el débito y el crédito se revierten")
    void shouldRollbackBalancesWhenPersistenceFails() throws Exception {
        // Forzamos un fallo al guardar el registro de la transacción (paso posterior al débito).
        doThrow(new RuntimeException("Fallo simulado al persistir la transacción"))
                .when(domainTransactionRepository).save(any());

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(new BigDecimal("200.00"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();
                    if (code < 500) {
                        throw new AssertionError("Se esperaba un error de servidor por el fallo simulado. Status: " + code);
                    }
                });

        // El saldo de AMBAS billeteras debe permanecer exactamente igual: nada se debitó ni acreditó.
        WalletEntity finalSender = walletRepository.findByUserId(sender.getId()).orElseThrow();
        WalletEntity finalReceiver = walletRepository.findByUserId(receiver.getId()).orElseThrow();

        assertEquals(0, SENDER_BALANCE.compareTo(finalSender.getBalance()),
                "El débito al emisor debe revertirse cuando la transacción falla");
        assertEquals(0, RECEIVER_BALANCE.compareTo(finalReceiver.getBalance()),
                "El crédito al receptor debe revertirse cuando la transacción falla");
    }
}
