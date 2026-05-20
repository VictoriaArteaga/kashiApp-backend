package com.backend.kashiapp.user.application.useCase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;

@DisplayName("BlockUserUseCase - TESTS")
class BlockUserUseCaseTest {

    private UserRepository userRepository;
    private BlockUserUseCase blockUserUseCase;

    private final String EMAIL = "test12@test.com";


    @BeforeEach
    void setup (){
        userRepository = mock(UserRepository.class);
        blockUserUseCase = new BlockUserUseCase(userRepository);
    }

    @Test
    void shouldBlockUserByEmail() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        blockUserUseCase.blockUserByEmail(EMAIL);
        
        // Se verifica que el estado se haya actualizado a BLOCKED
        assertEquals(AccountStatus.BLOCKED, user.getAccountStatus());


        // Se verifica que se haya guardado el usuario 
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        //verificar que no guarde el estado si el usuario no se encuentra 
        Exception ex = assertThrows(UserNotFoundException.class, () -> blockUserUseCase.blockUserByEmail(EMAIL));
        verify(userRepository, never()).save(any());
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));

    }

}