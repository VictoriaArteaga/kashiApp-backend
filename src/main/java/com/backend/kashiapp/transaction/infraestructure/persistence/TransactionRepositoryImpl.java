package com.backend.kashiapp.transaction.infraestructure.persistence;

import com.backend.kashiapp.transaction.application.mapper.TransactionMapper;

import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

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
}
