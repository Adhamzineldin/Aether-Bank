package com.maayn.cardservice.exception;

/**
 * Exception thrown when a card operation fails due to invalid card state or properties.
 */
public class InvalidCardException extends RuntimeException {

    private final String errorCode = "INVALID_CARD";

    public InvalidCardException(String message) {
        super(message);
    }

    public InvalidCardException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
