package com.backend.kashiapp.wallet.presentation.controller;


import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.common.response.ApiResponse;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.wallet.application.useCase.GetBalanceUseCase;
import com.backend.kashiapp.wallet.application.useCase.ToggleVisibilityUseCase;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final GetBalanceUseCase getBalanceUseCase;
    private final ToggleVisibilityUseCase toggleVisibilityUseCase;
    private final UserRepository userRepository;

    public WalletController(GetBalanceUseCase getBalanceUseCase,
                            ToggleVisibilityUseCase toggleVisibilityUseCase, UserRepository userRepository) {
        this.getBalanceUseCase = getBalanceUseCase;
        this.toggleVisibilityUseCase = toggleVisibilityUseCase;
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> getBalance(Authentication authentication) {
        // Extraemos el UUID real a partir de la autenticación
        UUID userId = extractUserIdFromPrincipal(authentication);
        return ResponseEntity.ok(ApiResponse.success(getBalanceUseCase.execute(userId)));
    }

    @PatchMapping("/visibility")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> toggleVisibility(Authentication authentication) {
        UUID userId = extractUserIdFromPrincipal(authentication);
        return ResponseEntity.ok(ApiResponse.success(toggleVisibilityUseCase.execute(userId)));
    }

    private UUID extractUserIdFromPrincipal(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Token de autenticación inválido o vacío");
        }
        // Según el JwtAuthFilter, el principal es el Email (String)
        String email = (String) authentication.getPrincipal();
        // Buscamos en la base de datos el UUID que corresponde a ese email
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No se encontró el usuario para el email: " + email))
                .getId();
    }
}
