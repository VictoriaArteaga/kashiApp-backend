package com.backend.kashiapp.user.application.useCase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.kashiapp.common.exception.AccountDeletedException;
import com.backend.kashiapp.common.exception.InvalidCredentialsException;
import com.backend.kashiapp.common.exception.UserNotFoundException;
import com.backend.kashiapp.user.application.dto.DeleteRequestDTO;
import com.backend.kashiapp.user.application.dto.DeleteResponseDTO;
import com.backend.kashiapp.user.domain.models.enums.AccountStatus;
import com.backend.kashiapp.user.domain.repository.UserRepository;

@Service
public class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DeleteUserUseCase (UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Crear el metodo para eliminar un usuario con el request de la contraseña y validar que la contraseña sea correcta antes de eliminar la cuenta
    public DeleteResponseDTO deleteUser(DeleteRequestDTO deleteRequestDTO) {
        var user = userRepository.findByEmail(deleteRequestDTO.getEmail()). 
        orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        //Validar contraseña
        if (passwordEncoder.matches(deleteRequestDTO.getPassword(), user.getPasswordHash())) {
            user.setAccountStatus(AccountStatus.DELETED);
            userRepository.save(user);
        } else {
            throw new InvalidCredentialsException("La contraseña es incorrecta");
        }

        //verificar que la cuenta no haya sido eliminada previamente
        if (user.getAccountStatus() == AccountStatus.DELETED) {
            throw new AccountDeletedException("La cuenta ya ha sido eliminada");
        }
        

        //Logica para eliminar el usuario, actualizar el estado de cuenta a eliminado 
        return new DeleteResponseDTO("Cuenta eliminada exitosamente");
    }

};