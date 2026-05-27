package com.backend.kashiapp.user.application.useCase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.UnlockAccountRequestDTO;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;


@DisplayName("UnlockAccountUseCase - TESTS")
class UnlockAccountUseCaseTest {
    private UnlockAccountUseCase unlockAccountUseCase;
    private UserRepository userRepository;

    private final String EMAIL = "test@example.com";


    @BeforeEach
    void setup(){
        userRepository = mock(UserRepository.class);
        unlockAccountUseCase = new UnlockAccountUseCase(userRepository);
    }

    @Test
    @DisplayName("Debe desbloquear la cuenta exitosamente")
    void shouldUnlockAccountSuccessfully() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setAccountStatus(AccountStatus.BLOCKED);

        UnlockAccountRequestDTO request = new UnlockAccountRequestDTO();
        request.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        unlockAccountUseCase.unlockAccount(request);
        // Verificar que el estado se haya actualizado a ACTIVE
        assertTrue(user.getAccountStatus() == AccountStatus.ACTIVE);
        verify(userRepository).save(user);

    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void shouldThrowExceptionWhenUserNotFound() {

        User user = new User();
        user.setEmail(EMAIL);
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UnlockAccountRequestDTO request = new UnlockAccountRequestDTO();
        request.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        
        Exception ex = assertThrows(UserNotFoundException.class, () -> {
            unlockAccountUseCase.unlockAccount(request);
        });

        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta no está bloqueada")
    void shouldThrowExceptionWhenAccountNotBlocked() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setAccountStatus(AccountStatus.ACTIVE);

        UnlockAccountRequestDTO request = new UnlockAccountRequestDTO();
        request.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        Exception ex = assertThrows(RuntimeException.class, () -> {
            unlockAccountUseCase.unlockAccount(request);
        });

        assertTrue(ex.getMessage().contains("La cuenta no está bloqueada"));
        verify(userRepository, never()).save(any(User.class));
    }
}