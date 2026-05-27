package com.backend.kashiapp.transaction.infraestructure.persistence;

import com.backend.kashiapp.common.response.PagedResult;
import com.backend.kashiapp.transaction.application.mapper.TransactionPersistenceMapper;

import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import com.backend.kashiapp.transaction.domain.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Implementación concreta del repositorio de dominio.
@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private static final int PAGE_SIZE = 15;

    private final JpaTransactionRepository
            jpaTransactionRepository;

    @Override
    public Transaction save(
            Transaction transaction
    ) {

        // Dominio -> Entidad -> Guardar -> Entidad guardada -> Dominio.
        TransactionEntity entity =
                TransactionPersistenceMapper.toEntity(
                        transaction
                );

        TransactionEntity saved =
                jpaTransactionRepository.save(
                        entity
                );

        return TransactionPersistenceMapper.toDomain(
                saved
        );
    }

    @Override
    public PagedResult<Transaction> findHistoryByWalletId(UUID walletId, int page) {

        PageRequest pageRequest = PageRequest.of(
                page,
                PAGE_SIZE,
                Sort.by("createdAt").descending()
        );

        Page<TransactionEntity> entityPage =
                jpaTransactionRepository.findHistoryByWalletId(
                        walletId,
                        TransactionType.OUTGOING,
                        TransactionType.INCOMING,
                        pageRequest
                );

        List<Transaction> content = entityPage.getContent()
                .stream()
                .map(TransactionPersistenceMapper::toDomain)
                .toList();

        return new PagedResult<>(
                content,
                page,
                entityPage.getTotalPages(),
                entityPage.getTotalElements()
        );
    }

    @Override
    public boolean existsRecentDuplicateTransfer(
            UUID senderWalletId,
            UUID receiverWalletId,
            BigDecimal amount,
            OffsetDateTime since
    ) {
        return jpaTransactionRepository.existsRecentDuplicate(
                senderWalletId,
                receiverWalletId,
                amount,
                TransactionType.OUTGOING,
                since
        );
    }
}
