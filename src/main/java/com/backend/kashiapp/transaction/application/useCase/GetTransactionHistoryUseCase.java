package com.backend.kashiapp.transaction.application.useCase;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.common.response.PagedResult;
import com.backend.kashiapp.transaction.application.dto.TransactionHistoryResponseDTO;
import com.backend.kashiapp.transaction.application.mapper.TransactionMapper;
import com.backend.kashiapp.transaction.application.port.UserQueryPort;
import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.repository.TransactionRepository;
import com.backend.kashiapp.wallet.domain.service.WalletService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetTransactionHistoryUseCase {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final UserQueryPort userQueryPort;

    public PagedResult<TransactionHistoryResponseDTO> execute(String email, int page) {

        UUID userId = resolveUserId(email);

        UUID walletId = walletService.getWalletByUserId(userId).getId();

        PagedResult<Transaction> transactions =
                transactionRepository.findHistoryByWalletId(walletId, page);

        List<TransactionHistoryResponseDTO> dtos = transactions.getContent()
                .stream()
                .map(TransactionMapper::toHistoryDTO)
                .toList();

        return new PagedResult<>(
                dtos,
                transactions.getPage(),
                transactions.getTotalPages(),
                transactions.getTotalElements()
        );
    }

    private UUID resolveUserId(String email) {
        return userQueryPort.findUserIdByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Usuario con email " + email + " no encontrado"
                        )
                );
    }
}
