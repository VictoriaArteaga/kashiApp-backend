package com.backend.kashiapp.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.kashiapp.user.application.dto.AuthResponse;
import com.backend.kashiapp.user.application.dto.LoginRequest;
import com.backend.kashiapp.user.application.useCase.LoginUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class UserController {
    
    private final LoginUseCase loginUseCase;

    public UserController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = loginUseCase.login(request);
        return ResponseEntity.ok(response);
    }
}