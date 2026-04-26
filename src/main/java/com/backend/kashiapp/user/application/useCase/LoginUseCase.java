package com.backend.kashiapp.user.application.useCase;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.kashiapp.user.application.dto.AuthResponseDTO;
import com.backend.kashiapp.user.application.dto.LoginRequestDTO;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.security.JwtService;

import com.backend.kashiapp.common.exception.UserNotFoundException;

@Service
public class LoginUseCase {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginUseCase(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UserNotFoundException("Contraseña incorrecta");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(token);
    }
}

