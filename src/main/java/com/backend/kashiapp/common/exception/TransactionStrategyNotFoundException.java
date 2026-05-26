package com.backend.kashiapp.common.exception;

public class TransactionStrategyNotFoundException extends RuntimeException{

    public TransactionStrategyNotFoundException(
            String message
    ) {
        super(message);
    }

}
