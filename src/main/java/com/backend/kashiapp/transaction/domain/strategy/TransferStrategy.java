package com.backend.kashiapp.transaction.domain.strategy;

import com.backend.kashiapp.common.exception.InsufficientFundsException;
import com.backend.kashiapp.common.exception.InvalidRecipientStateException;
import com.backend.kashiapp.common.exception.RecipientNotFoundException;
import com.backend.kashiapp.common.exception.SelfTransferNotAllowedException;

import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;

import com.backend.kashiapp.transaction.domain.models.enums.TransactionStatus;
import com.backend.kashiapp.transaction.domain.models.enums.TransactionType;

import com.backend.kashiapp.transaction.domain.models.Transaction;
import com.backend.kashiapp.transaction.domain.models.TransferCompletedEvent;
import com.backend.kashiapp.transaction.domain.repository.TransactionRepository;

import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;

import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.domain.service.WalletService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Estrategia encargada de ejecutar transferencias entre dos usuarios.
// Se hizo uso de @Transactional para garantizar la atomicidad del proceso de transacción.
@Component
@RequiredArgsConstructor
public class TransferStrategy
        implements TransactionStrategy {

    public static final String TYPE = "TRANSFER";

    private final WalletService walletService;

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    // Publica eventos de Spring que se procesan después del commit.
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String getType() {
        return TYPE;
    }

    // Si algo falla en cualquier paso, Spring revierte todo automáticamente.
    @Override
    @Transactional
    public TransactionResponseDTO execute(
            UUID senderUserId,
            TransactionRequestDTO request
    ) {

        // Validaciones previas a la transferencia.
        UserEntity recipient = findRecipient(
                request.getRecipientEmail()
        );

        validateNotSelfTransfer(
                senderUserId, recipient.getId()
        );

        validateRecipientStatus(recipient);

        // Obtenemos las billeteras de ambos usuarios.
        WalletResponseDTO senderWallet =
                walletService.getWalletByUserId(
                        senderUserId
                );

        WalletResponseDTO recipientWallet =
                walletService.getWalletByUserId(
                        recipient.getId()
                );

        validateSufficientFunds(
                senderWallet, request
        );

        // Débito al emisor y crédito al receptor.
        walletService.updateBalance(
                senderUserId,
                request.getAmount().negate()
        );

        walletService.updateBalance(
                recipient.getId(),
                request.getAmount()
        );

        // Ambas transacciones comparten la misma referencia para trazabilidad.
        UUID reference = UUID.randomUUID();

        saveTransaction(
                reference, senderWallet.getId(),
                recipientWallet.getId(),
                request, TransactionType.OUTGOING
        );

        saveTransaction(
                reference, senderWallet.getId(),
                recipientWallet.getId(),
                request, TransactionType.INCOMING
        );

        // Notifica a ambos usuarios solo si el commit fue exitoso.
        eventPublisher.publishEvent(
                new TransferCompletedEvent(
                        senderUserId,
                        recipient.getId(),
                        request.getAmount(),
                        reference
                )
        );

        return TransactionResponseDTO
                .builder()
                .transactionReference(reference)
                .amount(request.getAmount())
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    // Busca al destinatario por email o lanza excepción si no existe.
    private UserEntity findRecipient(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RecipientNotFoundException(
                                "Destinatario no encontrado"
                        )
                );
    }

    // No se permite que un usuario se transfiera a sí mismo.
    private void validateNotSelfTransfer(
            UUID senderId, UUID recipientId
    ) {

        if (recipientId.equals(senderId)) {
            throw new SelfTransferNotAllowedException(
                    "No puedes transferirte dinero a ti mismo"
            );
        }
    }

    // Solo se puede transferir a cuentas con estado ACTIVE.
    private void validateRecipientStatus(
            UserEntity recipient
    ) {

        if (recipient.getAccountStatus()
                != AccountStatus.ACTIVE) {

            throw new InvalidRecipientStateException(
                    "La cuenta del destinatario no está disponible"
            );
        }
    }

    // Verifica que el emisor tenga saldo suficiente antes de transferir.
    private void validateSufficientFunds(
            WalletResponseDTO senderWallet,
            TransactionRequestDTO request
    ) {

        if (senderWallet.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new InsufficientFundsException(
                    "Fondos insuficientes"
            );
        }
    }

    // Registra una entrada de auditoría en la tabla de transacciones.
    private void saveTransaction(
            UUID reference,
            UUID senderWalletId,
            UUID receiverWalletId,
            TransactionRequestDTO request,
            TransactionType type
    ) {

        Transaction transaction =
                Transaction.builder()
                        .transferReference(reference)
                        .senderWalletId(senderWalletId)
                        .receiverWalletId(receiverWalletId)
                        .amount(request.getAmount())
                        .type(type)
                        .status(TransactionStatus.COMPLETED)
                        .build();

        transactionRepository.save(transaction);
    }
}
