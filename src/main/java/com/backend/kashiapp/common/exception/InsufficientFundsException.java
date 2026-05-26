package com.backend.kashiapp.common.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(
            String message
    ) {
        super(message);
    }
}
