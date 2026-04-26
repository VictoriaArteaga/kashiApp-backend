package com.backend.kashiapp.common.exception;

public class AccountLockedException extends GlobalHeaderException {
    public AccountLockedException(String message) {
        super(message);
    }
}