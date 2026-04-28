package com.backend.kashiapp.common.exception;

public class PhoneNumberAlreadyExistsException extends GlobalHeaderException {
    // Constructor que acepta un mensaje de error
    public PhoneNumberAlreadyExistsException(String message) {
        super(message);
    }
    
}
