package com.backend.kashiapp.transaction.infraestructure.adapter;

import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserQueryAdapterTest {

    private UserRepository userRepository;
    private UserQueryAdapter adapter;

    private static final String EMAIL = "user@test.com";

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        adapter = new UserQueryAdapter(userRepository);
    }

    @Test
    void shouldReturnUserIdWhenEmailExists() {
        UUID expectedId = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setId(expectedId);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(entity));

        Optional<UUID> result = adapter.findUserIdByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertEquals(expectedId, result.get());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<UUID> result = adapter.findUserIdByEmail(EMAIL);

        assertTrue(result.isEmpty());
    }
}
