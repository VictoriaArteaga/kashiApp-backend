package com.backend.kashiapp.transaction.domain.repository;

import com.backend.kashiapp.common.response.PagedResult;
import com.backend.kashiapp.transaction.domain.models.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    PagedResult<Transaction> findHistoryByWalletId(UUID walletId, int page);

    // Indica si ya existe una transferencia saliente idéntica (mismo emisor, destinatario
    // y monto) creada a partir de 'since'. Se usa para descartar duplicados por doble clic.
    boolean existsRecentDuplicateTransfer(
            UUID senderWalletId,
            UUID receiverWalletId,
            BigDecimal amount,
            OffsetDateTime since
    );
}
