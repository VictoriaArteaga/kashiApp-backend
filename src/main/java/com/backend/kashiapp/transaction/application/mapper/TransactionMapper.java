package com.backend.kashiapp.transaction.application.mapper;

import com.backend.kashiapp.transaction.application.dto.TransactionHistoryResponseDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionHistoryType;
import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TransactionMapper {

    public static TransactionHistoryResponseDTO toHistoryDTO(Transaction transaction) {

        TransactionHistoryType type = transaction.getType() == TransactionType.OUTGOING
                ? TransactionHistoryType.SENT
                : TransactionHistoryType.RECEIVED;

        return TransactionHistoryResponseDTO.builder()
                .transferReference(transaction.getTransferReference())
                .type(type)
                .amount(transaction.getAmount())
                .date(transaction.getCreatedAt())
                .build();
    }
}
