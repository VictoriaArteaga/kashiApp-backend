package com.backend.kashiapp.common.exception;

public class RecipientNotFoundException extends RuntimeException {

    public RecipientNotFoundException(
            String message
    ) {
        super(message);
    }
}
