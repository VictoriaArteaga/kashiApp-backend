package com.backend.kashiapp.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.kashiapp.user.application.dto.UserProfileResponseDTO;
import com.backend.kashiapp.user.application.useCase.BlockUserUseCase;
import com.backend.kashiapp.user.application.useCase.GetUserUseCase;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController{
    private final GetUserUseCase getUserUseCase;
    private final BlockUserUseCase blockUserUseCase;

    public UserProfileController(GetUserUseCase getUserUseCase, BlockUserUseCase blockUserUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.blockUserUseCase = blockUserUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getCurrentUser () {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfileResponseDTO response = getUserUseCase.getCurrentUserProfile(email);
        return ResponseEntity.ok(response);
    }
 
    @PutMapping("/me/block")
    public ResponseEntity<Void> blockCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        blockUserUseCase.blockUserByEmail(email);
        return ResponseEntity.noContent().build();
    }
    
}