package com.backend.kashiapp.transaction.infraestructure.persistence;

import com.backend.kashiapp.transaction.application.mapper.TransactionMapper;

import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import com.backend.kashiapp.transaction.domain.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// Implementación concreta del repositorio de dominio.
@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final JpaTransactionRepository
            jpaTransactionRepository;

    @Override
    public Transaction save(
            Transaction transaction
    ) {

        // Dominio -> Entidad -> Guardar -> Entidad guardada -> Dominio.
        TransactionEntity entity =
                TransactionMapper.toEntity(
                        transaction
                );

        TransactionEntity saved =
                jpaTransactionRepository.save(
                        entity
                );

        return TransactionMapper.toDomain(
                saved
        );
    }

    @Override
    public List<Transaction> findHistoryByWalletId(UUID walletId) {

        return jpaTransactionRepository
                .findHistoryByWalletId(
                        walletId,
                        TransactionType.OUTGOING,
                        TransactionType.INCOMING
                )
                .stream()
                .map(TransactionMapper::toDomain)
                .toList();
    }
}
