package com.backend.kashiapp.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;
    // Constructor para inyectar el AuthService
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")

    // Endpoint para login de usuario que recibe un LoginRequest con email y password, valida las credenciales y devuelve un AuthResponse con el token JWT generado.
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

}