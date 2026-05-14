package com.backend.kashiapp.transaction.domain.service;

import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;

import java.util.UUID;


// Define las operaciones que el orquestador debe implementar.
public interface TransactionService {

    TransactionResponseDTO executeTransfer(
            UUID senderUserId,
            TransactionRequestDTO request
    );

}
