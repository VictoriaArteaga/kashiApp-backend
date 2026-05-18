package com.backend.kashiapp.transaction.infraestructure.adapter;

import com.backend.kashiapp.transaction.application.port.UserQueryPort;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserRepository userRepository;

    @Override
    public Optional<UUID> findUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getId());
    }
}
