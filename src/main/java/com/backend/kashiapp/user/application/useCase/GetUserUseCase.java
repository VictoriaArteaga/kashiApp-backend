package com.backend.kashiapp.user.application.useCase;

import org.springframework.stereotype.Service;

import com.backend.kashiapp.user.application.dto.UserProfileResponseDTO;
import com.backend.kashiapp.user.domain.repository.UserRepository;

@Service
public class GetUserUseCase {
    private final UserRepository userRepository;
    
    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponseDTO getCurrentUserProfile(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new UserProfileResponseDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getNumberPhone(),
                        user.getAccountStatus(),
                        user.getCreationDate()
                ))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

}