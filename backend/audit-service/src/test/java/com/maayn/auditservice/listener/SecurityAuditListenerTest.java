package com.maayn.auditservice.listener;

import com.maayn.auditservice.entity.AuditLog;
import com.maayn.auditservice.repository.AuditLogRepository;
import maayn.veld.generated.models.shared.AuditAction;
import maayn.veld.generated.models.shared.AuditEvent;
import maayn.veld.generated.models.shared.AuditStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityAuditListener Unit Tests — RabbitMQ Integration Simulation")
class SecurityAuditListenerTest {

    @Mock private AuditLogRepository repository;

    @InjectMocks private SecurityAuditListener listener;

    // ════════════════════════════════════════════════════════════════
    //  receiveAuditLog() — simulates other services sending audit events via RabbitMQ
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When Transaction Service sends TRANSFER_FUNDS audit event via RabbitMQ")
    class TransferFundsAuditTests {

        @Test
        @DisplayName("Should persist WARNING audit log for initiated transfer")
        void receiveAuditLog_transferInitiated_persistsWarning() {
            UUID userId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            AuditEvent event = new AuditEvent();
            event.setServiceName("transaction-service");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.WARNING);
            event.setDetails("Transfer TXN-ABC123 initiated. Amount: 100.00");
            event.setUserIdentifier(userId);
            event.setTimestamp(now);

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getServiceName()).isEqualTo("transaction-service");
            assertThat(saved.getAction()).isEqualTo(AuditAction.TRANSFER_FUNDS);
            assertThat(saved.getStatus()).isEqualTo(AuditStatus.WARNING);
            assertThat(saved.getDetails()).contains("TXN-ABC123");
            assertThat(saved.getDetails()).contains("initiated");
            assertThat(saved.getUserIdentifier()).isEqualTo(userId);
            assertThat(saved.getTimestamp()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should persist SUCCESS audit log for completed transfer")
        void receiveAuditLog_transferSuccess_persistsSuccess() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("transaction-service");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.SUCCESS);
            event.setDetails("Transfer TXN-DEF456 finalized with status SUCCESS");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getStatus()).isEqualTo(AuditStatus.SUCCESS);
        }

        @Test
        @DisplayName("Should persist FAILED audit log for failed transfer")
        void receiveAuditLog_transferFailed_persistsFailed() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("transaction-service");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.FAILED);
            event.setDetails("Transfer failed at initiation. Reason: Insufficient funds");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(AuditStatus.FAILED);
            assertThat(saved.getDetails()).contains("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("When IAM Service sends LOGIN_ATTEMPT audit event via RabbitMQ")
    class LoginAttemptAuditTests {

        @Test
        @DisplayName("Should persist SUCCESS audit log for successful login")
        void receiveAuditLog_loginSuccess() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("iam-service");
            event.setAction(AuditAction.LOGIN_ATTEMPT);
            event.setStatus(AuditStatus.SUCCESS);
            event.setDetails("User logged in successfully");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getServiceName()).isEqualTo("iam-service");
            assertThat(saved.getAction()).isEqualTo(AuditAction.LOGIN_ATTEMPT);
            assertThat(saved.getStatus()).isEqualTo(AuditStatus.SUCCESS);
        }

        @Test
        @DisplayName("Should persist UNAUTHORIZED audit log for failed login")
        void receiveAuditLog_loginFailed() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("iam-service");
            event.setAction(AuditAction.LOGIN_ATTEMPT);
            event.setStatus(AuditStatus.UNAUTHORIZED);
            event.setDetails("Invalid credentials for user@example.com");
            event.setUserIdentifier(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            event.setTimestamp(LocalDateTime.now());

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(AuditStatus.UNAUTHORIZED);
            assertThat(saved.getDetails()).contains("Invalid credentials");
        }
    }

    @Nested
    @DisplayName("When Account Service sends VIEW_ACCOUNT_BALANCE audit event via RabbitMQ")
    class ViewBalanceAuditTests {

        @Test
        @DisplayName("Should persist audit log for balance view")
        void receiveAuditLog_viewBalance() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("account-service");
            event.setAction(AuditAction.VIEW_ACCOUNT_BALANCE);
            event.setStatus(AuditStatus.SUCCESS);
            event.setDetails("User viewed balance for account XYZ");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            listener.receiveAuditLog(event);

            verify(repository).save(any(AuditLog.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should persist audit log with SYSTEM user ID (all zeros)")
        void receiveAuditLog_systemUser() {
            UUID systemId = UUID.fromString("00000000-0000-0000-0000-000000000000");

            AuditEvent event = new AuditEvent();
            event.setServiceName("transaction-service");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.WARNING);
            event.setDetails("SAGA reconciliation sweep");
            event.setUserIdentifier(systemId);
            event.setTimestamp(LocalDateTime.now());

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getUserIdentifier()).isEqualTo(systemId);
        }

        @Test
        @DisplayName("Should handle very long details string")
        void receiveAuditLog_longDetails() {
            String longDetails = "A".repeat(5000);

            AuditEvent event = new AuditEvent();
            event.setServiceName("transaction-service");
            event.setAction(AuditAction.TRANSFER_FUNDS);
            event.setStatus(AuditStatus.FAILED);
            event.setDetails(longDetails);
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getDetails()).hasSize(5000);
        }

        @Test
        @DisplayName("Should call repository.save exactly once per event")
        void receiveAuditLog_savesExactlyOnce() {
            AuditEvent event = new AuditEvent();
            event.setServiceName("test-service");
            event.setAction(AuditAction.DEPOSIT_FUNDS);
            event.setStatus(AuditStatus.SUCCESS);
            event.setDetails("Deposit of 500.00");
            event.setUserIdentifier(UUID.randomUUID());
            event.setTimestamp(LocalDateTime.now());

            listener.receiveAuditLog(event);

            verify(repository, times(1)).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Should persist all AuditAction types correctly")
        void receiveAuditLog_allActionTypes() {
            for (AuditAction action : AuditAction.values()) {
                AuditEvent event = new AuditEvent();
                event.setServiceName("test-service");
                event.setAction(action);
                event.setStatus(AuditStatus.SUCCESS);
                event.setDetails("Testing action: " + action.getValue());
                event.setUserIdentifier(UUID.randomUUID());
                event.setTimestamp(LocalDateTime.now());

                listener.receiveAuditLog(event);
            }

            verify(repository, times(AuditAction.values().length)).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Should persist all AuditStatus types correctly")
        void receiveAuditLog_allStatusTypes() {
            for (AuditStatus status : AuditStatus.values()) {
                AuditEvent event = new AuditEvent();
                event.setServiceName("test-service");
                event.setAction(AuditAction.TRANSFER_FUNDS);
                event.setStatus(status);
                event.setDetails("Testing status: " + status.getValue());
                event.setUserIdentifier(UUID.randomUUID());
                event.setTimestamp(LocalDateTime.now());

                listener.receiveAuditLog(event);
            }

            verify(repository, times(AuditStatus.values().length)).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Should handle multiple rapid audit events (burst scenario)")
        void receiveAuditLog_rapidBurst() {
            for (int i = 0; i < 100; i++) {
                AuditEvent event = new AuditEvent();
                event.setServiceName("load-test-service");
                event.setAction(AuditAction.TRANSFER_FUNDS);
                event.setStatus(AuditStatus.SUCCESS);
                event.setDetails("Burst event #" + i);
                event.setUserIdentifier(UUID.randomUUID());
                event.setTimestamp(LocalDateTime.now());

                listener.receiveAuditLog(event);
            }

            verify(repository, times(100)).save(any(AuditLog.class));
        }
    }
}

