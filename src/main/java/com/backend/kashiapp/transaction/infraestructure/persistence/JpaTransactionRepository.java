package com.backend.kashiapp.transaction.infraestructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Proporciona operaciones CRUD sobre la tabla de transacciones
public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
}
