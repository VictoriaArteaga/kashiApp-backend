package com.backend.kashiapp.user.application.useCase;


import java.time.Duration;
import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.kashiapp.common.exception.AccountLockedException;
import com.backend.kashiapp.common.exception.InvalidCredentialsException;
import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.AuthResponseDTO;
import com.backend.kashiapp.user.application.dto.LoginRequestDTO;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.user.infraestructure.security.JwtService;

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

    // Método que valida las credenciales del usuario y genera un token JWT si son correctas
    public AuthResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Verificar si la cuenta está bloqueada temporalmente
        if (user.getLockedUntil() != null && OffsetDateTime.now().isBefore(user.getLockedUntil())) {
            throw new AccountLockedException("Cuenta bloqueada temporalmente debido a múltiples intentos fallidos. Intente nuevamente más tarde.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Incrementar intentos fallidos
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= 5) {
                // Bloquear por 15 minutos
                user.setLockedUntil(OffsetDateTime.now().plus(Duration.ofMinutes(15)));
            }
            userRepository.save(user);
            throw new InvalidCredentialsException("Contraseña incorrecta");
        }

        // resetear intentos fallidos y desbloquear
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(token);
    }
}