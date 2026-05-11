package com.backend.kashiapp.wallet.presentation.controller;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.common.response.ApiResponse;
import com.backend.kashiapp.wallet.application.dto.WalletResponseDTO;
import com.backend.kashiapp.wallet.application.useCase.GetBalanceUseCase;
import com.backend.kashiapp.wallet.application.useCase.ToggleVisibilityUseCase;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final GetBalanceUseCase getBalanceUseCase;
    private final ToggleVisibilityUseCase toggleVisibilityUseCase;
    private final UserRepository userRepository;

    public WalletController(GetBalanceUseCase getBalanceUseCase,
                            ToggleVisibilityUseCase toggleVisibilityUseCase,
                            UserRepository userRepository) {
        this.getBalanceUseCase = getBalanceUseCase;
        this.toggleVisibilityUseCase = toggleVisibilityUseCase;
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> getBalance(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromToken(userDetails);
        return ResponseEntity.ok(ApiResponse.success(getBalanceUseCase.execute(userId)));
    }

    @PatchMapping("/visibility")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> toggleVisibility(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromToken(userDetails);
        return ResponseEntity.ok(ApiResponse.success(toggleVisibilityUseCase.execute(userId)));
    }

    private UUID getUserIdFromToken(UserDetails userDetails) {
        String email = userDetails.getUsername();
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(
                "User not found for email: " + email));
        return user.getId();
    }
}
