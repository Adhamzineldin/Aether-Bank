package com.maayn.financialservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class FinancialServiceExceptionHandler {

    @ExceptionHandler(FinancialOperationException.class)
    public ResponseEntity<?> handleFinancialOperationException(FinancialOperationException ex) {
        HttpStatus status = ex.getStatus();
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", ex.getMessage()
        ));
    }
}
