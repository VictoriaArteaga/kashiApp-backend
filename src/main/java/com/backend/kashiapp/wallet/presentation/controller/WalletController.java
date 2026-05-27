package com.backend.kashiapp.wallet.presentation.controller;


import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.common.response.ApiResponse;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.application.useCase.DepositMoneyUseCase;
import com.backend.kashiapp.wallet.application.useCase.GetBalanceUseCase;
import com.backend.kashiapp.wallet.application.useCase.ToggleVisibilityUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final GetBalanceUseCase getBalanceUseCase;
    private final ToggleVisibilityUseCase toggleVisibilityUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final UserRepository userRepository;

    public WalletController(GetBalanceUseCase getBalanceUseCase,
                            ToggleVisibilityUseCase toggleVisibilityUseCase,
                            DepositMoneyUseCase depositMoneyUseCase,
                            UserRepository userRepository) {
        this.getBalanceUseCase = getBalanceUseCase;
        this.toggleVisibilityUseCase = toggleVisibilityUseCase;
        this.depositMoneyUseCase = depositMoneyUseCase;
        this.userRepository = userRepository;
    }

    // Endpoint para recargar dinero.
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<String>> deposit(
            @AuthenticationPrincipal String email,
            @RequestBody BigDecimal amount) {


        UUID userId = getUserIdFromEmail(email);
        depositMoneyUseCase.execute(userId, amount);

        return ResponseEntity.ok(ApiResponse.success("Recarga realizada con éxito"));
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
        String email = (String) authentication.getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No se encontró el usuario para el email: " + email))
                .getId();
    }

    private UUID getUserIdFromEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"))
                .getId();
    }
}
