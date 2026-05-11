package com.backend.kashiapp.common.exception;

public class InvalidTokenException extends GlobalHeaderException {
    public InvalidTokenException(String message) {
        super(message);
    }
}