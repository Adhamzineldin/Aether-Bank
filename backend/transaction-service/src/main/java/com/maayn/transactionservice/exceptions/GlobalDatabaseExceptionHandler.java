package com.maayn.transactionservice.exceptions;

import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.ApiErrorResponse;
import maayn.veld.generated.errors.ApiException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalDatabaseExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        log.warn("Concurrent modification detected: {}", ex.getMessage());
        
        ApiException conflictException = new ApiException(
                "STATE_CONFLICT",
                HttpStatus.CONFLICT.value(),
                "The transaction was modified by another process. Please refresh and try again."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(conflictException.toErrorResponse());
    }
}