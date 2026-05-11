package com.backend.kashiapp.user.application.useCase;

import org.springframework.stereotype.Service;

import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.common.exception.UserNotFoundException;

@Service
public class BlockUserUseCase {
    // Implementación del caso de uso para bloquear a un usuario manualmente.
    private final UserRepository userRepository;

    public BlockUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void blockUserByEmail(String email) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
            
        user.setAccountStatus(AccountStatus.BLOCKED);
        userRepository.save(user);
    }
    
}