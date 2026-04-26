package com.backend.kashiapp.common.exception;

public class GlobalHeaderException extends RuntimeException {
    // Constructor que acepta un mensaje de error
    public GlobalHeaderException(String message) {
        super(message);
    }
}
