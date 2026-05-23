package com.backend.kashiapp.common.exception;

public class InvalidWalletBalanceException extends RuntimeException {

    public InvalidWalletBalanceException(String message) {
        super(message);
    }
}
