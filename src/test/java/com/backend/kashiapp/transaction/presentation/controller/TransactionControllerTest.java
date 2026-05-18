package com.backend.kashiapp.transaction.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.backend.kashiapp.common.response.ApiResponse;
import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;
import com.backend.kashiapp.transaction.application.useCase.ExecuteTransactionUseCase;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;

@DisplayName("TransactionController")
class TransactionControllerTest {

    private ExecuteTransactionUseCase executeTransactionUseCase;
    private TransactionController controller;

    private final String senderEmail = "sender@example.com";

    @BeforeEach
    void setup() {
        executeTransactionUseCase = mock(ExecuteTransactionUseCase.class);
        controller = new TransactionController(executeTransactionUseCase);
    }

    // Construye una petición con valores válidos.
    private TransactionRequestDTO buildRequest() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail("recipient@example.com");
        request.setAmount(new BigDecimal("100.00"));
        return request;
    }

    // Construye una respuesta del caso de uso usada cuando se espera éxito.
    private TransactionResponseDTO buildUseCaseResponse(BigDecimal amount) {
        return TransactionResponseDTO.builder()
                .transactionReference(UUID.randomUUID())
                .amount(amount)
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("Debe devolver 200 OK envolviendo la respuesta del caso de uso en ApiResponse.success")
    void shouldReturn200WithApiResponseSuccessOnTransfer() {
        TransactionRequestDTO request = buildRequest();
        TransactionResponseDTO useCaseResponse = buildUseCaseResponse(request.getAmount());

        when(executeTransactionUseCase.execute(senderEmail, request)).thenReturn(useCaseResponse);

        ResponseEntity<ApiResponse<TransactionResponseDTO>> response =
                controller.transfer(senderEmail, request);

        // El estado, el tipo de body y el payload envuelto deben coincidir con lo que devolvió el caso de uso.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isSameAs(useCaseResponse);
    }

    @Test
    @DisplayName("Debe reenviar email y request al caso de uso sin modificarlos")
    void shouldDelegateExactEmailAndRequestToUseCase() {
        TransactionRequestDTO request = buildRequest();
        when(executeTransactionUseCase.execute(senderEmail, request))
                .thenReturn(buildUseCaseResponse(request.getAmount()));

        controller.transfer(senderEmail, request);

        verify(executeTransactionUseCase).execute(senderEmail, request);
    }

    @Test
    @DisplayName("Debe devolver 401 Unauthorized cuando el principal del JWT es null")
    void shouldReturn401WhenEmailIsNull() {
        // Cuando el principal del JWT es null se debe responder 401 sin invocar al caso de uso.
        ResponseEntity<ApiResponse<TransactionResponseDTO>> response =
                controller.transfer(null, buildRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNull();

        // El caso de uso ni siquiera debe ser invocado.
        verify(executeTransactionUseCase, never()).execute(any(), any());
    }
}
