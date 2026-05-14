package com.backend.kashiapp.transaction.presentation.controller;

import com.backend.kashiapp.common.response.ApiResponse;
import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;
import com.backend.kashiapp.transaction.application.useCase.ExecuteTransactionUseCase;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final ExecuteTransactionUseCase
            executeTransactionUseCase;

    // El email del emisor se extrae automáticamente del token JWT.
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> transfer(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody TransactionRequestDTO request
    ) {

        // Si no hay usuario autenticado, rechazamos la petición.
        if (email == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        executeTransactionUseCase.execute(
                                email,
                                request
                        )
                )
        );
    }
}