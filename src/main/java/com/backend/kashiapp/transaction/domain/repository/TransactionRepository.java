 package com.backend.kashiapp.transaction.domain.repository;

import com.backend.kashiapp.transaction.domain.models.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findHistoryByWalletId(UUID walletId);
}
