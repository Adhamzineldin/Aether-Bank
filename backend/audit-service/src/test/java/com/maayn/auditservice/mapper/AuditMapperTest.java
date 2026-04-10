package com.maayn.auditservice.mapper;

import com.maayn.auditservice.entity.AuditLog;
import maayn.veld.generated.models.shared.AuditAction;
import maayn.veld.generated.models.shared.AuditEvent;
import maayn.veld.generated.models.shared.AuditStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuditMapper Unit Tests")
class AuditMapperTest {

    // ════════════════════════════════════════════════════════════════
    //  toEntity()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTests {

        @Test
        @DisplayName("Should map all fields from AuditEvent to AuditLog entity")
        void toEntity_mapsAllFields() {
            UUID userId = UUID.randomUUID();
            LocalDateTime timestamp = LocalDateTime.of(2025, 6, 15, 10, 30, 0);

            AuditEvent event = new AuditEvent();
            event.setServiceName("transaction-service");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.SUCCESS);
            event.setDetails("Transfer TXN-ABC completed");
            event.setUserIdentifier(userId);
            event.setTimestamp(timestamp);

            AuditLog log = AuditMapper.toEntity(event);

            assertThat(log.getServiceName()).isEqualTo("transaction-service");
            assertThat(log.getAction()).isEqualTo(AuditAction.TRANSFER_FUNDS);
            assertThat(log.getStatus()).isEqualTo(AuditStatus.SUCCESS);
            assertThat(log.getDetails()).isEqualTo("Transfer TXN-ABC completed");
            assertThat(log.getUserIdentifier()).isEqualTo(userId);
            assertThat(log.getTimestamp()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("Should map WARNING status correctly")
        void toEntity_warningStatus() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("transaction-service");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.WARNING);
            event.setDetails("Transfer initiated");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            AuditLog log = AuditMapper.toEntity(event);

            assertThat(log.getStatus()).isEqualTo(AuditStatus.WARNING);
        }

        @Test
        @DisplayName("Should map FAILED status correctly")
        void toEntity_failedStatus() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("transaction-service");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.FAILED);
            event.setDetails("Insufficient funds");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            AuditLog log = AuditMapper.toEntity(event);

            assertThat(log.getStatus()).isEqualTo(AuditStatus.FAILED);
        }

        @Test
        @DisplayName("Should map UNAUTHORIZED status correctly")
        void toEntity_unauthorizedStatus() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("iam-service");
            event.setAction(AuditAction.LOGIN_ATTEMPT);
            event.setStatus(AuditStatus.UNAUTHORIZED);
            event.setDetails("Bad credentials");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            AuditLog log = AuditMapper.toEntity(event);

            assertThat(log.getStatus()).isEqualTo(AuditStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should map all AuditAction values correctly")
        void toEntity_allActions() {
            for (AuditAction action : AuditAction.values()) {
                AuditEvent event = new AuditEvent();
                event.setServiceName("test-service");
                event.setAction(action);
                event.setStatus(AuditStatus.SUCCESS);
                event.setDetails("Testing " + action);
                event.setUserIdentifier(UUID.randomUUID());
                event.setTimestamp(LocalDateTime.now());

                AuditLog log = AuditMapper.toEntity(event);
                assertThat(log.getAction()).isEqualTo(action);
            }
        }

        @Test
        @DisplayName("Should not set MongoDB id (left for MongoDB auto-generation)")
        void toEntity_idIsNull() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("test");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.SUCCESS);
            event.setDetails("test");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            AuditLog log = AuditMapper.toEntity(event);

            assertThat(log.getId()).isNull();
        }

        @Test
        @DisplayName("Should preserve exact timestamp from event")
        void toEntity_preservesTimestamp() {
            LocalDateTime precise = LocalDateTime.of(2025, 12, 31, 23, 59, 59, 999999999);

            AuditEvent event = new AuditEvent();
            event.setServiceName("test");
            event.setAction(AuditAction.DEPOSIT_FUNDS);
            event.setStatus(AuditStatus.SUCCESS);
            event.setDetails("test");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(precise);

            AuditLog log = AuditMapper.toEntity(event);

            assertThat(log.getTimestamp()).isEqualTo(precise);
        }

        @Test
        @DisplayName("Should handle SYSTEM user ID (all zeros)")
        void toEntity_systemUserId() {
            UUID systemId = UUID.fromString("00000000-0000-0000-0000-000000000000");

            AuditEvent event = new AuditEvent();
            event.setServiceName("transaction-service");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.WARNING);
            event.setDetails("SAGA reconciliation");
            event.setUserIdentifier(systemId);
            event.setTimestamp(LocalDateTime.now());

            AuditLog log = AuditMapper.toEntity(event);

            assertThat(log.getUserIdentifier()).isEqualTo(systemId);
        }

        @Test
        @DisplayName("Should handle empty details string")
        void toEntity_emptyDetails() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("test");
            event.setAction(AuditAction.WITHDRAW_FUNDS);
            event.setStatus(AuditStatus.SUCCESS);
            event.setDetails("");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            AuditLog log = AuditMapper.toEntity(event);

            assertThat(log.getDetails()).isEmpty();
        }

        @Test
        @DisplayName("Should handle very long service name")
        void toEntity_longServiceName() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("a".repeat(500));
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.SUCCESS);
            event.setDetails("test");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            AuditLog log = AuditMapper.toEntity(event);

            assertThat(log.getServiceName()).hasSize(500);
        }
    }
}

