package com.backend.kashiapp.common.exception;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.backend.kashiapp.common.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePhoneNumberAlreadyExists(PhoneNumberAlreadyExistsException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getMessage(),
            OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.LOCKED.value(),
            ex.getMessage(),
            OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.LOCKED).body(response);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getMessage(),
            OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccountDeletedException.class)
    public ResponseEntity<ErrorResponse> handleAccountDeleted(AccountDeletedException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.GONE.value(), //gone es el status code adecuado para indicar que el recurso ya no está disponible
            ex.getMessage(), //el mensaje de error se obtiene de la excepción personalizada
            OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.GONE).body(response);
    }
}
