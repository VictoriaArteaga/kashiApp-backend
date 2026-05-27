package com.backend.kashiapp.integration;

import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Historial de transacciones: GET /api/v1/transactions/history")
public class TransactionHistoryIntegrationTest extends BaseIntegrationTest {

    private UserEntity sender;
    private UserEntity receiver;
    private String senderToken;
    private String receiverToken;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        sender = persistActiveUser("hist_sender@test.com", "hist_sender", "1212121212", "ID-HIST-SENDER");
        persistWallet(sender.getId(), new BigDecimal("1000.00"));

        receiver = persistActiveUser("hist_receiver@test.com", "hist_receiver", "3434343434", "ID-HIST-RECEIVER");
        persistWallet(receiver.getId(), new BigDecimal("500.00"));

        senderToken = jwtService.generateToken(sender.getEmail());
        receiverToken = jwtService.generateToken(receiver.getEmail());
    }

    // Ejecuta una transferencia del emisor al receptor por el monto indicado.
    private void doTransfer(BigDecimal amount) throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail(receiver.getEmail());
        request.setAmount(amount);

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("El historial del emisor muestra el movimiento como SENT")
    void senderHistoryShowsOutgoingAsSent() throws Exception {
        doTransfer(new BigDecimal("150.00"));

        mockMvc.perform(get("/api/v1/transactions/history")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].type").value("SENT"))
                .andExpect(jsonPath("$.data.content[0].amount").value(150.00))
                .andExpect(jsonPath("$.data.content[0].transferReference").isNotEmpty());
    }

    @Test
    @DisplayName("El historial del receptor muestra el movimiento como RECEIVED")
    void receiverHistoryShowsIncomingAsReceived() throws Exception {
        doTransfer(new BigDecimal("150.00"));

        mockMvc.perform(get("/api/v1/transactions/history")
                        .header("Authorization", "Bearer " + receiverToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].type").value("RECEIVED"))
                .andExpect(jsonPath("$.data.content[0].amount").value(150.00));
    }

    @Test
    @DisplayName("Varias transferencias aparecen todas en el historial del emisor")
    void multipleTransfersAppearInHistory() throws Exception {
        // Montos distintos para no activar la deduplicación de duplicados.
        doTransfer(new BigDecimal("10.00"));
        doTransfer(new BigDecimal("20.00"));
        doTransfer(new BigDecimal("30.00"));

        mockMvc.perform(get("/api/v1/transactions/history")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content", hasSize(3)));
    }

    @Test
    @DisplayName("Un usuario sin movimientos obtiene un historial vacío")
    void historyIsEmptyWhenNoTransactions() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/history")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("El historial exige autenticación: sin JWT responde 401/403")
    void historyRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();
                    if (code != 401 && code != 403) {
                        throw new AssertionError("El historial sin JWT debe rechazarse. Status: " + code);
                    }
                });
    }
}
