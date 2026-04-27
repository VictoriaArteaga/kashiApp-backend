package com.backend.kashiapp.user.application.useCase;

import org.springframework.stereotype.Service;

import com.backend.kashiapp.user.application.dto.AuthResponseDTO;
import com.backend.kashiapp.user.domain.repository.Token2FARepository;
import com.backend.kashiapp.user.domain.repository.UserRepository;
import com.backend.kashiapp.user.infraestructure.security.JwtService;



@Service
public class VerifyOptUseCase {
    
    private final UserRepository userRepository;
    private final Token2FARepository token2FARepository;
    private final JwtService jwtService;
    //constructor que inyecta las dependencias de UserRepository y Token2FARepository
    public VerifyOptUseCase(UserRepository userRepository, Token2FARepository token2FARepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.token2FARepository = token2FARepository;
        this.jwtService = jwtService;
    }

    // Método que verifica el token OTP para un usuario dado su correo electrónico
    public AuthResponseDTO verifyOpt(String email, String otp) {
        // Buscar el usuario
        var user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        //Buscar el token OTP
        var tokenEntityOpt = token2FARepository.findByUserId(user.get().getId())
            .orElseThrow(() -> new RuntimeException("Token OTP no encontrado para el usuario"));

        //Verificar si el token OTP no ha expirado
        if (tokenEntityOpt.getExpirationTime().isBefore(java.time.OffsetDateTime.now())) {
            token2FARepository.delete(tokenEntityOpt);
            throw new RuntimeException("Token OTP expirado");
        }

        //Verificar si el token OTP es correcto
        if (!tokenEntityOpt.getToken().equals(otp)) {
            throw new RuntimeException("Token OTP incorrecto");
        }

        //Eliminar el token y generar un token JWT para el usuario
        token2FARepository.delete(tokenEntityOpt);
        String token = jwtService.generateToken(user.get().getEmail());
        return new AuthResponseDTO(token);
 
    }
}