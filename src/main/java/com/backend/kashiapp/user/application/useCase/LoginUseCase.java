package com.backend.kashiapp.user.application.useCase;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
import com.backend.kashiapp.user.infraestructure.persistence.Token2FAEntity;
import com.backend.kashiapp.user.infraestructure.security.EmailService;

@Service
public class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Token2FARepository token2FARepository;
    private final EmailService emailService;
    private final FailedAttemptService failedAttemptService;

    public LoginUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder,
            Token2FARepository token2FARepository, EmailService emailService,
            FailedAttemptService failedAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.token2FARepository = token2FARepository;
        this.emailService = emailService;
        this.failedAttemptService = failedAttemptService;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        // Verificar si la cuenta está bloqueada temporalmente por intentos fallidos
        if (user.getLockedUntil() != null && OffsetDateTime.now().isBefore(user.getLockedUntil())) {
            throw new AccountLockedException("Cuenta bloqueada temporalmente debido a múltiples intentos fallidos. Intente nuevamente más tarde.");
        }
        // Si el lock ya expiró, resetear los intentos para que el usuario recupere sus 5 intentos
        if (user.getLockedUntil() != null && !OffsetDateTime.now().isBefore(user.getLockedUntil())) {
            try {
                failedAttemptService.resetFailedAttempts(user.getId());
            } catch (Exception e) {
        
            }
        }
        // Verificar si la cuenta ha sido eliminada
        if (user.getAccountStatus() == AccountStatus.DELETED) {
            throw new AccountDeletedException("La cuenta ha sido eliminada");
        }

        // Validar la contraseña y registrar el intento fallido si es incorrecta
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            try {
                failedAttemptService.recordFailedAttempt(user.getId());
            } catch (Exception e) {
                // Si falla el registro del intento, no debe afectar la respuesta al usuario
            }
            throw new InvalidCredentialsException("Contraseña incorrecta");
        }

        //eliminar cualquier token 2FA existente para el usuario antes de generar uno nuevo
        token2FARepository.deleteAllByUserId(user.getId());

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));

        // Crear una nueva entidad de token 2FA y guardarla en la base de datos
        var tokenEntity = new Token2FAEntity();
        tokenEntity.setUser(user);
        tokenEntity.setToken(otp);
        tokenEntity.setExpirationTime(OffsetDateTime.now().plus(Duration.ofMinutes(10)));
        token2FARepository.save(tokenEntity);

        //resetear los intentos fallidos después de un inicio de sesión exitoso
        failedAttemptService.resetFailedAttempts(user.getId());

        emailService.sendOptEmail(user.getEmail(), otp);

        return new AuthResponseDTO("OTP enviado a tu correo electronico.");
    }
}
