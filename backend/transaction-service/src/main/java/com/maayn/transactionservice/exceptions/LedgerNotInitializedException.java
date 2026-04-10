package com.maayn.transactionservice.exceptions;

public class LedgerNotInitializedException extends RuntimeException {
    public LedgerNotInitializedException(String message) {
        super(message);
    }
}