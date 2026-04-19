package com.maayn.cardservice.exception;

/**
 * Exception thrown when a card operation fails due to insufficient balance/credit.
 */
public class InsufficientBalanceException extends RuntimeException {

    private final String errorCode = "INSUFFICIENT_BALANCE";

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
