package com.backend.kashiapp.transaction.domain.repository;

import com.backend.kashiapp.common.response.PagedResult;
import com.backend.kashiapp.transaction.domain.models.Transaction;

import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    PagedResult<Transaction> findHistoryByWalletId(UUID walletId, int page);
}
