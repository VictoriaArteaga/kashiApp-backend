package com.backend.kashiapp.common.exception;

public class InvalidCredentialsException extends GlobalHeaderException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}