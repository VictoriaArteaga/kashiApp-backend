package com.backend.kashiapp.common.exception;

public class SelfTransferNotAllowedException extends RuntimeException{

    public SelfTransferNotAllowedException(
            String message
    ) {
        super(message);
    }
}
