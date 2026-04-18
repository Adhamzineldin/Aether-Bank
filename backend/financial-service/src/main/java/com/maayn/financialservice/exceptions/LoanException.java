package com.maayn.financialservice.exceptions;

import org.springframework.http.HttpStatus;

public class LoanException extends RuntimeException {

    private final HttpStatus status;

    public LoanException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}