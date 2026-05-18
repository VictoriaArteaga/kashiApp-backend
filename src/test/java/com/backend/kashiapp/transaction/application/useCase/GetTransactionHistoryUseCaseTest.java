package com.backend.kashiapp.transaction.application.useCase;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.common.response.PagedResult;
import com.backend.kashiapp.transaction.application.dto.TransactionHistoryResponseDTO;
import com.backend.kashiapp.transaction.application.port.UserQueryPort;
import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import com.backend.kashiapp.transaction.domain.repository.TransactionRepository;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GetTransactionHistoryUseCase")
class GetTransactionHistoryUseCaseTest {

    private UserQueryPort userQueryPort;
    private WalletService walletService;
    private TransactionRepository transactionRepository;
    private GetTransactionHistoryUseCase useCase;

    private static final String EMAIL = "user@test.com";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID WALLET_ID = UUID.randomUUID();

    @BeforeEach
    void setup() {
        userQueryPort = mock(UserQueryPort.class);
        walletService = mock(WalletService.class);
        transactionRepository = mock(TransactionRepository.class);
        useCase = new GetTransactionHistoryUseCase(transactionRepository, walletService, userQueryPort);
    }

    private PagedResult<Transaction> buildPagedTransaction(int page, int totalPages, long totalElements) {
        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .transferReference(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.OUTGOING)
                .status(TransactionStatus.COMPLETED)
                .createdAt(OffsetDateTime.now())
                .build();

        return new PagedResult<>(List.of(tx), page, totalPages, totalElements);
    }

    private void mockHappyPath() {
        WalletResponseDTO wallet = new WalletResponseDTO();
        wallet.setId(WALLET_ID);

        when(userQueryPort.findUserIdByEmail(EMAIL)).thenReturn(Optional.of(USER_ID));
        when(walletService.getWalletByUserId(USER_ID)).thenReturn(wallet);
        when(transactionRepository.findHistoryByWalletId(WALLET_ID, 0))
                .thenReturn(buildPagedTransaction(0, 1, 1L));
    }

    @Test
    @DisplayName("Debe devolver exactamente una transacción cuando hay una en el repositorio")
    void shouldReturnOneTransaction() {
        mockHappyPath();
        PagedResult<TransactionHistoryResponseDTO> result = useCase.execute(EMAIL, 0);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Debe devolver el número de página correcto")
    void shouldReturnCorrectPageNumber() {
        mockHappyPath();
        PagedResult<TransactionHistoryResponseDTO> result = useCase.execute(EMAIL, 0);
        assertEquals(0, result.getPage());
    }

    @Test
    @DisplayName("Debe devolver el total de páginas correcto")
    void shouldReturnCorrectTotalPages() {
        mockHappyPath();
        PagedResult<TransactionHistoryResponseDTO> result = useCase.execute(EMAIL, 0);
        assertEquals(1, result.getTotalPages());
    }

    @Test
    @DisplayName("Debe devolver el total de elementos correcto")
    void shouldReturnCorrectTotalElements() {
        mockHappyPath();
        PagedResult<TransactionHistoryResponseDTO> result = useCase.execute(EMAIL, 0);
        assertEquals(1L, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundException si el email no corresponde a ningún usuario")
    void shouldThrowWhenUserNotFound() {
        when(userQueryPort.findUserIdByEmail(EMAIL)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> useCase.execute(EMAIL, 0));
    }

    @Test
    @DisplayName("Debe devolver contenido vacío cuando no hay transacciones")
    void shouldReturnEmptyContentWhenNoTransactions() {
        WalletResponseDTO wallet = new WalletResponseDTO();
        wallet.setId(WALLET_ID);

        when(userQueryPort.findUserIdByEmail(EMAIL)).thenReturn(Optional.of(USER_ID));
        when(walletService.getWalletByUserId(USER_ID)).thenReturn(wallet);
        when(transactionRepository.findHistoryByWalletId(WALLET_ID, 0))
                .thenReturn(new PagedResult<>(List.of(), 0, 0, 0L));

        assertTrue(useCase.execute(EMAIL, 0).getContent().isEmpty());
    }

    @Test
    @DisplayName("Debe devolver cero elementos totales cuando no hay transacciones")
    void shouldReturnZeroTotalElementsWhenNoTransactions() {
        WalletResponseDTO wallet = new WalletResponseDTO();
        wallet.setId(WALLET_ID);

        when(userQueryPort.findUserIdByEmail(EMAIL)).thenReturn(Optional.of(USER_ID));
        when(walletService.getWalletByUserId(USER_ID)).thenReturn(wallet);
        when(transactionRepository.findHistoryByWalletId(WALLET_ID, 0))
                .thenReturn(new PagedResult<>(List.of(), 0, 0, 0L));

        assertEquals(0L, useCase.execute(EMAIL, 0).getTotalElements());
    }
}
