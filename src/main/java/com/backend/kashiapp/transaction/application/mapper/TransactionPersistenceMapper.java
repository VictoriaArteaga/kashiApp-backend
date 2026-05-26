package com.backend.kashiapp.transaction.application.mapper;

import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.infraestructure.persistence.TransactionEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TransactionPersistenceMapper {

    public static Transaction toDomain(TransactionEntity entity) {

        return Transaction.builder()
                .id(entity.getId())
                .transferReference(entity.getTransferReference())
                .senderWalletId(entity.getSenderWalletId())
                .receiverWalletId(entity.getReceiverWalletId())
                .amount(entity.getAmount())
                .type(entity.getType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static TransactionEntity toEntity(Transaction transaction) {

        TransactionEntity entity = new TransactionEntity();
        entity.setId(transaction.getId());
        entity.setTransferReference(transaction.getTransferReference());
        entity.setSenderWalletId(transaction.getSenderWalletId());
        entity.setReceiverWalletId(transaction.getReceiverWalletId());
        entity.setAmount(transaction.getAmount());
        entity.setType(transaction.getType());
        entity.setStatus(transaction.getStatus());
        entity.setCreatedAt(transaction.getCreatedAt());
        return entity;
    }
}
