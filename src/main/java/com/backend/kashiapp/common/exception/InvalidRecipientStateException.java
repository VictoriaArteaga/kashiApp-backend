package com.backend.kashiapp.common.exception;

public class InvalidRecipientStateException extends RuntimeException{

    public InvalidRecipientStateException(
            String message
    ) {
        super(message);
    }
}
