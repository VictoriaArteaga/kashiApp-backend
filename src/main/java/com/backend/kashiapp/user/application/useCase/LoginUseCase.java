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
import com.backend.kashiapp.user.domain.repository.Token2FARepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.user.infraestructure.security.EmailService;

@Service
public class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Token2FARepository token2FARepository;
    private final EmailService emailService;

    public LoginUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, Token2FARepository token2FARepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.token2FARepository = token2FARepository;
        this.emailService = emailService;
    }

    // Método que valida las credenciales del usuario y genera un token JWT si son correctas
    public AuthResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Verificar si la cuenta está bloqueada temporalmente
        if (user.getLockedUntil() != null && OffsetDateTime.now().isBefore(user.getLockedUntil())) {
            throw new AccountLockedException("Cuenta bloqueada temporalmente debido a múltiples intentos fallidos. Intente nuevamente más tarde.");
        }

        // Verificar la contraseña
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

        //Generar token OTP para 2FA
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));

        var tokenEntity = new com.backend.kashiapp.user.infraestructure.persistence.Token2FAEntity();
        tokenEntity.setUser(user);
        tokenEntity.setToken(otp);
        tokenEntity.setExpirationTime(OffsetDateTime.now().plus(Duration.ofMinutes(10)));

        token2FARepository.save(tokenEntity);


        // resetear intentos fallidos y desbloquear
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        //llamar al servicio de correo para enviar el OTP
        emailService.sendOptEmail(user.getEmail(), otp);

        return new AuthResponseDTO("OTP enviado a tu correo electronico.");
    }
}