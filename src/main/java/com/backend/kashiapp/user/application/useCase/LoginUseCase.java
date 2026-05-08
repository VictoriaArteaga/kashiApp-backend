package com.backend.kashiapp.user.application.useCase;


import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.backend.kashiapp.common.exception.AccountDeletedException;
import com.backend.kashiapp.common.exception.AccountLockedException;
import com.backend.kashiapp.common.exception.InvalidCredentialsException;
import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.AuthResponseDTO;
import com.backend.kashiapp.user.application.dto.LoginRequestDTO;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
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

    // Método privado para registrar intentos fallidos
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void recordFailedAttempt(UUID userId) {
        // Recuperar el usuario fresh en la nueva transacción
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= 5) {
            user.setLockedUntil(OffsetDateTime.now().plus(Duration.ofMinutes(15)));
        }
        userRepository.save(user);
    }

    @Transactional
    // Método que valida las credenciales del usuario y genera un token JWT si son correctas
    public AuthResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Verificar si la cuenta está bloqueada temporalmente
        if (user.getLockedUntil() != null && OffsetDateTime.now().isBefore(user.getLockedUntil())) {
            throw new AccountLockedException("Cuenta bloqueada temporalmente debido a múltiples intentos fallidos. Intente nuevamente más tarde.");
        } else if (user.getAccountStatus() == AccountStatus.DELETED){
            throw new AccountDeletedException("La cuenta ha sido eliminada");
        }
        
        // Verificar la contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Registrar intento fallido en transacción separada
            recordFailedAttempt(user.getId());
        
            throw new InvalidCredentialsException("Contraseña incorrecta");
        }

        // Eliminar cualquier token OTP previo antes de generar uno nuevo
        token2FARepository.deleteAllByUserId(user.getId());

        // Generar token OTP para 2FA
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));

        var tokenEntity = new com.backend.kashiapp.user.infraestructure.persistence.Token2FAEntity();
        tokenEntity.setUser(user);
        tokenEntity.setToken(otp);
        tokenEntity.setExpirationTime(OffsetDateTime.now().plus(Duration.ofMinutes(10)));

        token2FARepository.save(tokenEntity);

        // resetear intentos fallidos y desbloquear cuando el login es exitoso
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    
        //llamar al servicio de correo para enviar el OTP
        emailService.sendOptEmail(user.getEmail(), otp);

        return new AuthResponseDTO("OTP enviado a tu correo electronico.");
    }
}