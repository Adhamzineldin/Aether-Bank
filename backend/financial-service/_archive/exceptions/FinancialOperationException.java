package com.maayn.financialservice.exceptions;

import org.springframework.http.HttpStatus;

public class FinancialOperationException extends RuntimeException {
    private final HttpStatus status;

    public FinancialOperationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
