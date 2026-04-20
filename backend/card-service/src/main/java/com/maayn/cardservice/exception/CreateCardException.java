package com.maayn.cardservice.exception;

/**
 * Exception thrown when card issuance/creation fails.
 * Used for validation errors, business rule violations, and persistence errors.
 * 
 * Extends RuntimeException for unchecked exception handling,
 * aligned with transaction service error handling pattern.
 */
public class CreateCardException extends RuntimeException {

    private final String errorCode;

    public CreateCardException(String message) {
        super(message);
        this.errorCode = "CREATE_CARD_ERROR";
    }

    public CreateCardException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CreateCardException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CREATE_CARD_ERROR";
    }

    public CreateCardException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

