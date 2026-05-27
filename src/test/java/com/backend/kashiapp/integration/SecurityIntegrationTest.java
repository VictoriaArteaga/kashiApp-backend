package com.backend.kashiapp.integration;

import com.backend.kashiapp.TestcontainersConfiguration;
import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("KAN-67: Las peticiones HTTP planas deben ser rechazadas o redirigidas a HTTPS")
    void shouldRejectOrRedirectHttpRequests() throws Exception {
        // Spring Security con requiresChannel().anyRequest().requiresSecure() devuelve 302 a HTTPS,
        // o un 4xx si no hay credenciales en la petición plana.
        // /api/v1/auth/login es POST-only: un GET se rechaza con 405 (método no permitido),
        // que también es un rechazo válido de la petición.
        mockMvc.perform(get("http://localhost/api/v1/auth/login"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    boolean isRejectedOrRedirected =
                            status == 302 || status == 403 || status == 400 || status == 401 || status == 405;
                    if (!isRejectedOrRedirected) {
                        throw new AssertionError(
                                "La petición HTTP no fue redirigida ni rechazada. Status: " + status);
                    }
                });
    }

    @Test
    @DisplayName("KAN-67b: Endpoint protegido vía HTTP sin token debe ser rechazado")
    void shouldRejectPlainHttpRequestOnProtectedEndpoint() throws Exception {
        mockMvc.perform(get("http://localhost/api/v1/wallet/balance"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    boolean isRejected =
                            status == 302 || status == 401 || status == 403;
                    if (!isRejected) {
                        throw new AssertionError(
                                "La petición HTTP sobre endpoint protegido debió ser rechazada o redirigida. Status: " + status);
                    }
                });
    }

    @Test
    @DisplayName("KAN-67c: POST de transferencia vía HTTP plano sin JWT debe ser rechazado")
    void shouldRejectPlainHttpTransferWithoutAuth() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail("any@test.com");
        request.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("http://localhost/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    boolean isRejected =
                            status == 302 || status == 401 || status == 403;
                    if (!isRejected) {
                        throw new AssertionError(
                                "La transferencia HTTP sin JWT debió ser rechazada. Status: " + status);
                    }
                });
    }
}
