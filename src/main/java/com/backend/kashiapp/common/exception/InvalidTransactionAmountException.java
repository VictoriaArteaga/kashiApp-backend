package com.backend.kashiapp.common.exception;

public class InvalidTransactionAmountException extends RuntimeException{

    public InvalidTransactionAmountException(
            String message
    ) {
        super(message);
    }

}
