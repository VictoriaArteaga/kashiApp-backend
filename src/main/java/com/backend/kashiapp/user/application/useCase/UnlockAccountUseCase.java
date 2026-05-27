package com.backend.kashiapp.user.application.useCase;

import org.springframework.stereotype.Service;

import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.UnlockAccountRequestDTO;
import com.backend.kashiapp.user.application.dto.UnlockAccountResponseDTO;
import com.backend.kashiapp.user.domain.repository.UserRepository;

@Service
public class UnlockAccountUseCase {
    private final UserRepository userRepository;

    public UnlockAccountUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UnlockAccountResponseDTO unlockAccount(UnlockAccountRequestDTO request) {
        var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!user.isBlocked()) {
            throw new RuntimeException("La cuenta no está bloqueada");
        }

        if (user.isBlocked()) {
            user.unlock();
            userRepository.save(user);
        }
        

        return new UnlockAccountResponseDTO("Cuenta desbloqueada exitosamente");
    }
}