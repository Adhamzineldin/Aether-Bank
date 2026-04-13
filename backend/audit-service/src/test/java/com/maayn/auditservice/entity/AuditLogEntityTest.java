package com.maayn.auditservice.entity;

import maayn.veld.generated.models.shared.AuditAction;
import maayn.veld.generated.models.shared.AuditStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuditLog Entity Unit Tests")
class AuditLogEntityTest {

    @Nested
    @DisplayName("Setters and Getters")
    class SetterGetterTests {

        @Test
        @DisplayName("Should get and set all fields correctly")
        void allFieldsSetAndGet() {
            AuditLog log = new AuditLog();
            UUID userId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            log.setId("mongo-id-123");
            log.setServiceName("transaction-service");
            log.setAction(AuditAction.TRANSFER_FUNDS);
            log.setStatus(AuditStatus.SUCCESS);
            log.setDetails("Transfer completed");
            log.setUserIdentifier(userId);
            log.setTimestamp(now);

            assertThat(log.getId()).isEqualTo("mongo-id-123");
            assertThat(log.getServiceName()).isEqualTo("transaction-service");
            assertThat(log.getAction()).isEqualTo(AuditAction.TRANSFER_FUNDS);
            assertThat(log.getStatus()).isEqualTo(AuditStatus.SUCCESS);
            assertThat(log.getDetails()).isEqualTo("Transfer completed");
            assertThat(log.getUserIdentifier()).isEqualTo(userId);
            assertThat(log.getTimestamp()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should have null id before persistence")
        void newEntity_nullId() {
            AuditLog log = new AuditLog();
            assertThat(log.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle null fields gracefully")
        void nullFields() {
            AuditLog log = new AuditLog();
            assertThat(log.getServiceName()).isNull();
            assertThat(log.getAction()).isNull();
            assertThat(log.getStatus()).isNull();
            assertThat(log.getDetails()).isNull();
            assertThat(log.getUserIdentifier()).isNull();
            assertThat(log.getTimestamp()).isNull();
        }
    }

    @Nested
    @DisplayName("All AuditAction Values")
    class AuditActionTests {

        @Test
        @DisplayName("Should support TRANSFER_FUNDS action")
        void transferFundsAction() {
            AuditLog log = new AuditLog();
            log.setAction(AuditAction.TRANSFER_FUNDS);
            assertThat(log.getAction()).isEqualTo(AuditAction.TRANSFER_FUNDS);
        }

        @Test
        @DisplayName("Should support DEPOSIT_FUNDS action")
        void depositFundsAction() {
            AuditLog log = new AuditLog();
            log.setAction(AuditAction.DEPOSIT_FUNDS);
            assertThat(log.getAction()).isEqualTo(AuditAction.DEPOSIT_FUNDS);
        }

        @Test
        @DisplayName("Should support WITHDRAW_FUNDS action")
        void withdrawFundsAction() {
            AuditLog log = new AuditLog();
            log.setAction(AuditAction.WITHDRAW_FUNDS);
            assertThat(log.getAction()).isEqualTo(AuditAction.WITHDRAW_FUNDS);
        }

        @Test
        @DisplayName("Should support VIEW_ACCOUNT_BALANCE action")
        void viewBalanceAction() {
            AuditLog log = new AuditLog();
            log.setAction(AuditAction.VIEW_ACCOUNT_BALANCE);
            assertThat(log.getAction()).isEqualTo(AuditAction.VIEW_ACCOUNT_BALANCE);
        }

        @Test
        @DisplayName("Should support LOGIN_ATTEMPT action")
        void loginAttemptAction() {
            AuditLog log = new AuditLog();
            log.setAction(AuditAction.LOGIN_ATTEMPT);
            assertThat(log.getAction()).isEqualTo(AuditAction.LOGIN_ATTEMPT);
        }
    }

    @Nested
    @DisplayName("All AuditStatus Values")
    class AuditStatusTests {

        @Test
        @DisplayName("Should support SUCCESS status")
        void successStatus() {
            AuditLog log = new AuditLog();
            log.setStatus(AuditStatus.SUCCESS);
            assertThat(log.getStatus()).isEqualTo(AuditStatus.SUCCESS);
        }

        @Test
        @DisplayName("Should support FAILED status")
        void failedStatus() {
            AuditLog log = new AuditLog();
            log.setStatus(AuditStatus.FAILED);
            assertThat(log.getStatus()).isEqualTo(AuditStatus.FAILED);
        }

        @Test
        @DisplayName("Should support WARNING status")
        void warningStatus() {
            AuditLog log = new AuditLog();
            log.setStatus(AuditStatus.WARNING);
            assertThat(log.getStatus()).isEqualTo(AuditStatus.WARNING);
        }

        @Test
        @DisplayName("Should support UNAUTHORIZED status")
        void unauthorizedStatus() {
            AuditLog log = new AuditLog();
            log.setStatus(AuditStatus.UNAUTHORIZED);
            assertThat(log.getStatus()).isEqualTo(AuditStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("Equality (Lombok @Data)")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void equalWhenFieldsMatch() {
            UUID userId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            AuditLog log1 = new AuditLog();
            log1.setId("same-id");
            log1.setServiceName("svc");
            log1.setAction(AuditAction.TRANSFER_FUNDS);
            log1.setStatus(AuditStatus.SUCCESS);
            log1.setDetails("details");
            log1.setUserIdentifier(userId);
            log1.setTimestamp(now);

            AuditLog log2 = new AuditLog();
            log2.setId("same-id");
            log2.setServiceName("svc");
            log2.setAction(AuditAction.TRANSFER_FUNDS);
            log2.setStatus(AuditStatus.SUCCESS);
            log2.setDetails("details");
            log2.setUserIdentifier(userId);
            log2.setTimestamp(now);

            assertThat(log1).isEqualTo(log2);
            assertThat(log1.hashCode()).isEqualTo(log2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when status differs")
        void notEqualWhenStatusDiffers() {
            AuditLog log1 = new AuditLog();
            log1.setStatus(AuditStatus.SUCCESS);

            AuditLog log2 = new AuditLog();
            log2.setStatus(AuditStatus.FAILED);

            assertThat(log1).isNotEqualTo(log2);
        }
    }
}

