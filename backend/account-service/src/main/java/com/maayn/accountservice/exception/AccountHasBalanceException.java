package com.maayn.accountservice.exception;

public class AccountHasBalanceException extends RuntimeException {
    public AccountHasBalanceException(String message) {
        super(message);
    }
}

