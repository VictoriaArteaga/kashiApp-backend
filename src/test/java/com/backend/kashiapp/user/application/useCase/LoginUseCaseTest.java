package com.backend.kashiapp.user.application.useCase;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class LoginUseCaseTest {
    private LoginUseCase loginUseCase;
    private UserRepository userRepository;
    private FailedAttemptService failedAttemptService;
    private Token2FARepository token2FARepository;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;

    private final UUID USER_ID = UUID.randomUUID();
    private final String EMAIL = "test@example.com";
    private final String PASSWORD = "password";
    private final String PASSWORD_HASH = "passwordHash";
    private final String WRONG_PASSWORD = "wrongPassword";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        failedAttemptService = mock(FailedAttemptService.class);
        token2FARepository = mock(Token2FARepository.class);
        emailService = mock(EmailService.class);
        passwordEncoder = mock(PasswordEncoder.class);

        loginUseCase = new LoginUseCase(
            userRepository,
            passwordEncoder,
            token2FARepository,
            emailService,
            failedAttemptService
        );
    }

    @Test
    void shouldLoginSuccessfullyAndSendOTP() {
        UserEntity user = new UserEntity();
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setId(USER_ID);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

        //El usuario ingresa la contraseña correcta
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        //Se genera un OTP
        AuthResponseDTO response = loginUseCase.login(request);

        //Se espera el envio del codigo OTP y se verifica que se guarde el token 2FA
        assertEquals("OTP enviado a tu correo electronico.", response.getToken());
        verify(token2FARepository).save(any());

        //Se espera que se reseteen los intentos fallidos 
        verify(failedAttemptService).resetFailedAttempts(USER_ID);
        verify(emailService).sendOptEmail(eq(EMAIL), anyString());
    }

    @Test
    void shouldFailLoginWithWrongPassword() {
        UserEntity user = new UserEntity();
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setId(USER_ID);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        //El usuario ingresa la contraseña incorrecta
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(WRONG_PASSWORD);

        //Se espera que se lance una excepción de credenciales inválidas
        Exception ex = assertThrows(InvalidCredentialsException.class, () -> loginUseCase.login(request));
        assertEquals("Contraseña incorrecta", ex.getMessage());

        //Se espera que se incremente el contador de intentos fallidos
        verify(failedAttemptService).recordFailedAttempt(USER_ID);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        //El usuario ingresa un correo no registrado
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        //Se espera que se lance una excepción de usuario no encontrado
        Exception ex = assertThrows(UserNotFoundException.class, () -> loginUseCase.login(request));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }
    @Test
    void shouldThrowExceptionWhenAccountDeleted() {
        UserEntity user = new UserEntity();
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setAccountStatus(AccountStatus.DELETED);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

        //El usuario intenta iniciar sesión con una cuenta eliminada
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        //Se espera que se lance una excepción de cuenta eliminada
        Exception ex = assertThrows(AccountDeletedException.class, () -> loginUseCase.login(request));
        assertEquals("La cuenta ha sido eliminada", ex.getMessage());
    }

    @Test
    void shouldThrowWhenAccountIsLocked() {
        UserEntity user = new UserEntity();
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setLockedUntil(java.time.OffsetDateTime.now().plusMinutes(10)); // Cuenta bloqueada por 10 minutos

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

        //El usuario intenta iniciar sesión mientras la cuenta está bloqueada
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        //Se espera que se lance una excepción de cuenta bloqueada
        Exception ex = assertThrows(AccountLockedException.class, () -> loginUseCase.login(request));
        assertEquals("Cuenta bloqueada temporalmente debido a múltiples intentos fallidos. Intente nuevamente más tarde.", ex.getMessage());
    }
}