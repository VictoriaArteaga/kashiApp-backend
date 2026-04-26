package com.backend.kashiapp.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.kashiapp.user.application.dto.AuthResponseDTO;
import com.backend.kashiapp.user.application.dto.LoginRequestDTO;
import com.backend.kashiapp.user.application.dto.UserRequestDTO;
import com.backend.kashiapp.user.application.dto.UserResponseDTO;
import com.backend.kashiapp.user.application.useCase.LoginUseCase;
import com.backend.kashiapp.user.application.useCase.RegisterUserUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {
    
    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;

    public UserController(LoginUseCase loginUseCase, RegisterUserUseCase registerUserUseCase) {
        this.loginUseCase = loginUseCase;
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = loginUseCase.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO response = registerUserUseCase.register(request);
        return ResponseEntity.status(201).body(response);
    }
}