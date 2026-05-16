package com.backend.kashiapp.transaction.application.mapper;

import com.backend.kashiapp.transaction.application.dto.TransactionHistoryResponseDTO;
import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;
import com.backend.kashiapp.transaction.infraestructure.persistence.TransactionEntity;

import lombok.experimental.UtilityClass;

// @UtilityClass: Convierte entre el modelo de dominio y la entidad JPA de transacciones.
@UtilityClass
public class TransactionMapper {

    // Convierte la entidad de base de datos al modelo de dominio.
    public static Transaction toDomain(
            TransactionEntity entity
    ) {

        return Transaction.builder()
                .id(
                        entity.getId()
                )
                .transferReference(
                        entity.getTransferReference()
                )
                .senderWalletId(
                        entity.getSenderWalletId()
                )
                .receiverWalletId(
                        entity.getReceiverWalletId()
                )
                .amount(
                        entity.getAmount()
                )
                .type(
                        entity.getType()
                )
                .status(
                        entity.getStatus()
                )
                .createdAt(
                        entity.getCreatedAt()
                )
                .build();
    }

    // Convierte el modelo de dominio a entidad de persistencia.
    public static TransactionEntity toEntity(
            Transaction transaction
    ) {

        TransactionEntity entity =
                new TransactionEntity();

        entity.setId(
                transaction.getId()
        );

        entity.setTransferReference(
                transaction.getTransferReference()
        );

        entity.setSenderWalletId(
                transaction.getSenderWalletId()
        );

        entity.setReceiverWalletId(
                transaction.getReceiverWalletId()
        );

        entity.setAmount(
                transaction.getAmount()
        );

        entity.setType(
                transaction.getType()
        );

        entity.setStatus(
                transaction.getStatus()
        );

        entity.setCreatedAt(
                transaction.getCreatedAt()
        );

        return entity;
    }

    // Convierte el modelo de dominio al DTO de historial del usuario.
    public static TransactionHistoryResponseDTO toHistoryDTO(Transaction transaction) {

        // OUTGOING = el usuario envió; INCOMING = el usuario recibió
        String type = transaction.getType() == TransactionType.OUTGOING ? "SENT" : "RECEIVED";

        return TransactionHistoryResponseDTO.builder()
                .transferReference(transaction.getTransferReference())
                .type(type)
                .amount(transaction.getAmount())
                .date(transaction.getCreatedAt())
                .build();
    }
}
