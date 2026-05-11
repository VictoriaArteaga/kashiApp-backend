package com.backend.kashiapp.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.kashiapp.user.application.dto.UserProfileResponseDTO;
import com.backend.kashiapp.user.application.dto.DeleteRequestDTO;
import com.backend.kashiapp.user.application.dto.DeleteResponseDTO;
import com.backend.kashiapp.user.application.useCase.BlockUserUseCase;
import com.backend.kashiapp.user.application.useCase.DeleteUserUseCase;
import com.backend.kashiapp.user.application.useCase.GetUserUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController{
    private final GetUserUseCase getUserUseCase;
    private final BlockUserUseCase blockUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserProfileController(GetUserUseCase getUserUseCase, BlockUserUseCase blockUserUseCase, DeleteUserUseCase deleteUserUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.blockUserUseCase = blockUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
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

    @DeleteMapping("/me/delete")
    public ResponseEntity<DeleteResponseDTO> deleteCurrentUser(@Valid @RequestBody DeleteRequestDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        request.setEmail(email); //Se toma el email del security context para evitar eliminar cuentas ajenas
        DeleteResponseDTO response = deleteUserUseCase.deleteUser(request);
        return ResponseEntity.ok(response);
    }
}
