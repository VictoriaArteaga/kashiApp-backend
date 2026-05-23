package com.backend.kashiapp.transaction.application.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.service.TransactionService;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;

@DisplayName("ExecuteTransactionUseCase")
class ExecuteTransactionUseCaseTest {

    private TransactionService transactionService;
    private UserRepository userRepository;
    private ExecuteTransactionUseCase executeTransactionUseCase;

    private final UUID senderId = UUID.randomUUID();
    private final String senderEmail = "sender@example.com";

    @BeforeEach
    void setup() {
        transactionService = mock(TransactionService.class);
        userRepository = mock(UserRepository.class);
        executeTransactionUseCase = new ExecuteTransactionUseCase(transactionService, userRepository);
    }

    // Construye una petición mínima usada en todos los tests.
    private TransactionRequestDTO buildRequest() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setRecipientEmail("recipient@example.com");
        request.setAmount(new BigDecimal("100.00"));
        return request;
    }

    // Construye un usuario válido en estado ACTIVE.
    private UserEntity buildSender() {
        UserEntity sender = new UserEntity();
        sender.setId(senderId);
        sender.setEmail(senderEmail);
        return sender;
    }

    @Test
    @DisplayName("Debe traducir el email del JWT al id del usuario y llamar al service con él")
    void shouldResolveSenderIdAndDelegateToService() {
        TransactionRequestDTO request = buildRequest();
        TransactionResponseDTO serviceResponse = TransactionResponseDTO.builder()
                .transactionReference(UUID.randomUUID())
                .amount(request.getAmount())
                .status(TransactionStatus.COMPLETED)
                .build();

        when(userRepository.findByEmail(senderEmail)).thenReturn(Optional.of(buildSender()));
        when(transactionService.executeTransfer(senderId, request)).thenReturn(serviceResponse);

        executeTransactionUseCase.execute(senderEmail, request);

        // Se debe consultar el repositorio por email e invocar al service con el id resuelto.
        verify(userRepository).findByEmail(senderEmail);
        verify(transactionService).executeTransfer(senderId, request);
    }

    @Test
    @DisplayName("Debe devolver la respuesta del service sin transformarla (pass-through)")
    void shouldReturnExactResponseFromService() {
        TransactionRequestDTO request = buildRequest();
        TransactionResponseDTO serviceResponse = TransactionResponseDTO.builder()
                .transactionReference(UUID.randomUUID())
                .amount(request.getAmount())
                .status(TransactionStatus.COMPLETED)
                .build();

        when(userRepository.findByEmail(senderEmail)).thenReturn(Optional.of(buildSender()));
        when(transactionService.executeTransfer(senderId, request)).thenReturn(serviceResponse);

        TransactionResponseDTO result = executeTransactionUseCase.execute(senderEmail, request);

        // Debe devolverse la misma instancia, aquí no se permite copia ni transformación de datos.
        assertThat(result).isSameAs(serviceResponse);
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundException si el email del JWT no existe")
    void shouldThrowUserNotFoundExceptionWhenEmailDoesNotExist() {
        // Si el email del JWT no se puede resolver a un usuario, no se puede continuar con la transferencia.
        when(userRepository.findByEmail(senderEmail)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(
                UserNotFoundException.class,
                () -> executeTransactionUseCase.execute(senderEmail, buildRequest())
        );

        // El mensaje debe incluir el email.
        assertThat(ex.getMessage()).isEqualTo("Usuario con email " + senderEmail + " no encontrado");
    }
}
