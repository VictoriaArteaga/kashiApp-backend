package com.backend.kashiapp.transaction.presentation.controller;

import com.backend.kashiapp.common.response.ApiResponse;
import com.backend.kashiapp.common.response.PagedResult;
import com.backend.kashiapp.transaction.application.dto.TransactionHistoryResponseDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionRequestDTO;
import com.backend.kashiapp.transaction.application.dto.TransactionResponseDTO;
import com.backend.kashiapp.transaction.application.useCase.ExecuteTransactionUseCase;
import com.backend.kashiapp.transaction.application.useCase.GetTransactionHistoryUseCase;

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

    private final GetTransactionHistoryUseCase
            getTransactionHistoryUseCase;

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

    // Retorna el historial de movimientos del usuario autenticado (15 por página).
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagedResult<TransactionHistoryResponseDTO>>> history(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "0") int page
    ) {

        if (email == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        getTransactionHistoryUseCase.execute(email, page)
                )
        );
    }
}