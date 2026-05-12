package com.backend.kashiapp.user.application.useCase;

import java.util.UUID;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;



class FailedAttemptServiceTest {
    private FailedAttemptService failedAttemptService;
    private UserRepository userRepository;

    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        failedAttemptService = new FailedAttemptService(userRepository);
    }

    @Test
    void shouldIncrementFailedAttempts() {
        UserEntity user = new UserEntity();
        user.setId(USER_ID);
        user.setFailedAttempts(2);
        user.setLockedUntil(null);

        when(userRepository.findById(USER_ID)).thenReturn(java.util.Optional.of(user));
    
        //se registra un intento fallido
        failedAttemptService.recordFailedAttempt(USER_ID);
        assertEquals(3, user.getFailedAttempts());
        assertNotNull(user.getLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void shouldLockAccountAfter5FailedAttempts(){
        UserEntity user = new UserEntity();
        user.setId(USER_ID);
        user.setFailedAttempts(4);
        user.setLockedUntil(null);

        when(userRepository.findById(USER_ID)).thenReturn(java.util.Optional.of(user));
    
        //se registra un intento fallido
        failedAttemptService.recordFailedAttempt(USER_ID);
        assertEquals(5, user.getFailedAttempts());
        assertNotNull(user.getLockedUntil());
        assertTrue(user.getLockedUntil().isAfter(java.time.OffsetDateTime.now()));
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(java.util.Optional.empty());
    
        Exception ex = assertThrows(UserNotFoundException.class, () -> failedAttemptService.recordFailedAttempt(USER_ID));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }   
}