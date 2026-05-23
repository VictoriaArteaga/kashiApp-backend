package com.backend.kashiapp.common.exception;

public class InvalidWalletStateException extends RuntimeException {

    public InvalidWalletStateException(String message) {
        super(message);
    }
}
