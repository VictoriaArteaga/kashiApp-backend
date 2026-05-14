package com.backend.kashiapp.transaction.domain.repository;

import com.backend.kashiapp.transaction.domain.models.Transaction;

public interface TransactionRepository {

    Transaction save(Transaction transaction);
}
