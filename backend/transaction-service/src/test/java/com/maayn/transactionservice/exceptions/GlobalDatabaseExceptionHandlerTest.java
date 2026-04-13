package com.maayn.transactionservice.exceptions;

import maayn.veld.generated.errors.ApiErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GlobalDatabaseExceptionHandler Unit Tests")
class GlobalDatabaseExceptionHandlerTest {

    private final GlobalDatabaseExceptionHandler handler = new GlobalDatabaseExceptionHandler();

    @Test
    @DisplayName("Should return 409 CONFLICT for optimistic locking failure")
    void handleOptimisticLockingFailure_returnsConflict() {
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException("Transaction", "Concurrent modification");

        ResponseEntity<ApiErrorResponse> response = handler.handleOptimisticLockingFailure(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("STATE_CONFLICT");
        assertThat(response.getBody().error()).contains("modified by another process");
    }

    @Test
    @DisplayName("Should include STATE_CONFLICT error code in response body")
    void handleOptimisticLockingFailure_errorCode() {
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException("Transaction", "Conflict");

        ResponseEntity<ApiErrorResponse> response = handler.handleOptimisticLockingFailure(ex);

        assertThat(response.getBody().code()).isEqualTo("STATE_CONFLICT");
    }

    @Test
    @DisplayName("Should include 409 status in response body")
    void handleOptimisticLockingFailure_status409() {
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException("Transaction", "Conflict");

        ResponseEntity<ApiErrorResponse> response = handler.handleOptimisticLockingFailure(ex);

        assertThat(response.getBody().status()).isEqualTo(409);
    }
}

