package com.backend.kashiapp.user.application.useCase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.kashiapp.common.exception.AccountLockedException;
import com.backend.kashiapp.common.exception.InvalidCredentialsException;
import com.backend.kashiapp.user.application.dto.LoginRequestDTO;
import com.backend.kashiapp.user.domain.repository.Token2FARepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import com.backend.kashiapp.user.infraestructure.security.EmailService;

class LoginUseCaseTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private Token2FARepository token2FARepository;
    private EmailService emailService;

    private LoginUseCase loginUseCase;

    private final String EMAIL = "test@test.com";
    private final String WRONG_PASSWORD = "wrongpass";
    private final String CORRECT_PASSWORD = "123456";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        token2FARepository = mock(Token2FARepository.class);
        emailService = mock(EmailService.class);

        loginUseCase = new LoginUseCase(
                userRepository,
                passwordEncoder,
                token2FARepository,
                emailService
        );
    }

    @Test
    void shouldLockUserAfterFiveFailedAttempts() {

        UserEntity user = new UserEntity();
        user.setEmail(EMAIL);
        user.setPasswordHash("hashed");
        user.setFailedAttempts(4);
        user.setLockedUntil(null);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, "hashed")).thenReturn(false);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(WRONG_PASSWORD);

        // Ejecutar intento que provoca bloqueo.
        assertThrows(InvalidCredentialsException.class, () -> {
            loginUseCase.login(request);
        });

        // Validaciones
        assertEquals(5, user.getFailedAttempts());
        assertNotNull(user.getLockedUntil());
        assertTrue(user.getLockedUntil().isAfter(OffsetDateTime.now()));

        verify(userRepository, atLeastOnce()).save(user);
    }

    @Test
    void shouldThrowExceptionWhenAccountIsLocked() {

        UserEntity user = new UserEntity();
        user.setEmail(EMAIL);
        user.setPasswordHash("hashed");
        user.setFailedAttempts(5);
        user.setLockedUntil(OffsetDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(WRONG_PASSWORD);

        assertThrows(AccountLockedException.class, () -> {
            loginUseCase.login(request);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldResetAttemptsOnSuccessfulLogin() {

        UserEntity user = new UserEntity();
        user.setEmail(EMAIL);
        user.setPasswordHash("hashed");
        user.setFailedAttempts(3);
        user.setLockedUntil(null);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CORRECT_PASSWORD, "hashed")).thenReturn(true);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(EMAIL);
        request.setPassword(CORRECT_PASSWORD);

        loginUseCase.login(request);

        assertEquals(0, user.getFailedAttempts());
        assertNull(user.getLockedUntil());

        verify(userRepository).save(user);
        verify(token2FARepository).save(any());
        verify(emailService).sendOptEmail(eq(EMAIL), anyString());
    }
}