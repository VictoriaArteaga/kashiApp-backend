package com.backend.kashiapp.user.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.kashiapp.user.application.dto.AuthResponseDTO;
import com.backend.kashiapp.user.application.dto.ForgotPasswordRequestDTO;
import com.backend.kashiapp.user.application.dto.ForgotPasswordResponseDTO;
import com.backend.kashiapp.user.application.dto.LoginRequestDTO;
import com.backend.kashiapp.user.application.dto.ResetPasswordRequestDTO;
import com.backend.kashiapp.user.application.dto.ResetPasswordResponseDTO;
import com.backend.kashiapp.user.application.dto.UnlockAccountRequestDTO;
import com.backend.kashiapp.user.application.dto.UnlockAccountResponseDTO;
import com.backend.kashiapp.user.application.dto.UserRequestDTO;
import com.backend.kashiapp.user.application.dto.UserResponseDTO;
import com.backend.kashiapp.user.application.dto.VerifyOptRequestDTO;
import com.backend.kashiapp.user.application.useCase.ForgotPasswordUseCase;
import com.backend.kashiapp.user.application.useCase.LoginUseCase;
import com.backend.kashiapp.user.application.useCase.RegisterUserUseCase;
import com.backend.kashiapp.user.application.useCase.ResetPasswordUseCase;
import com.backend.kashiapp.user.application.useCase.UnlockAccountUseCase;
import com.backend.kashiapp.user.application.useCase.VerifyOptUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {
    
    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyOptUseCase verifyOptUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final UnlockAccountUseCase unlockAccountUseCase;
    public UserController(LoginUseCase loginUseCase, 
            RegisterUserUseCase registerUserUseCase, 
            VerifyOptUseCase verifyOptUseCase, 
            ForgotPasswordUseCase forgotPasswordUseCase, 
            ResetPasswordUseCase resetPasswordUseCase,
            UnlockAccountUseCase unlockAccountUseCase) {

        this.loginUseCase = loginUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.verifyOptUseCase = verifyOptUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.unlockAccountUseCase = unlockAccountUseCase;
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

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDTO> verifyOtp(@Valid @RequestBody VerifyOptRequestDTO request) {
        AuthResponseDTO response = verifyOptUseCase.verifyOpt(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        ForgotPasswordResponseDTO response = forgotPasswordUseCase.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        ResetPasswordResponseDTO response = resetPasswordUseCase.restablecerContraseña(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unlock-account")
    public ResponseEntity<UnlockAccountResponseDTO> unlockAccount(@Valid @RequestBody UnlockAccountRequestDTO request) {
        UnlockAccountResponseDTO response = unlockAccountUseCase.unlockAccount(request);
        return ResponseEntity.ok(response);
    }

}