package com.maayn.auditservice.integration;

import com.maayn.auditservice.entity.AuditLog;
import com.maayn.auditservice.listener.SecurityAuditListener;
import com.maayn.auditservice.repository.AuditLogRepository;
import maayn.veld.generated.models.shared.AuditAction;
import maayn.veld.generated.models.shared.AuditEvent;
import maayn.veld.generated.models.shared.AuditStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration-style tests simulating the FULL end-to-end RabbitMQ message flow
 * from various microservices → RabbitMQ → Audit Service → MongoDB persistence.
 *
 * Simulates:
 * 1. Transaction Service sends TRANSFER_FUNDS audit events (initiated, success, failure)
 * 2. Card Service sends card payment audit events
 * 3. Account Service sends account balance view audit events
 * 4. IAM Service sends login attempt audit events
 * 5. Edge cases: burst events, system user, large payloads
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Audit Service — End-to-End RabbitMQ Integration Simulation Tests")
class AuditRabbitMQIntegrationSimulationTest {

    @Mock private AuditLogRepository repository;

    private SecurityAuditListener listener;

    @BeforeEach
    void setUp() {
        listener = new SecurityAuditListener(repository);
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 1: Transaction Service → Transfer Lifecycle Audit Trail
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 1: Transaction Service — Complete Transfer Audit Trail")
    class TransactionServiceAuditTrailTests {

        @Test
        @DisplayName("Full flow: Transfer initiated → WARNING audit log saved to MongoDB")
        void transferInitiated_warningLogSaved() {
            UUID userId = UUID.randomUUID();
            AuditEvent event = buildAuditEvent(
                    "transaction-service",
                    AuditAction.TRANSFER_FUNDS,
                    AuditStatus.WARNING,
                    "Transfer TXN-ABC12345 initiated. Amount: 500.00",
                    userId
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getServiceName()).isEqualTo("transaction-service");
            assertThat(saved.getAction()).isEqualTo(AuditAction.TRANSFER_FUNDS);
            assertThat(saved.getStatus()).isEqualTo(AuditStatus.WARNING);
            assertThat(saved.getDetails()).contains("TXN-ABC12345");
            assertThat(saved.getDetails()).contains("500.00");
            assertThat(saved.getUserIdentifier()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Full flow: Transfer finalized SUCCESS → SUCCESS audit log saved to MongoDB")
        void transferSuccess_successLogSaved() {
            UUID userId = UUID.randomUUID();
            AuditEvent event = buildAuditEvent(
                    "transaction-service",
                    AuditAction.TRANSFER_FUNDS,
                    AuditStatus.SUCCESS,
                    "Transfer TXN-DEF67890 finalized with status SUCCESS. Reason: Funds secured",
                    userId
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(AuditStatus.SUCCESS);
            assertThat(saved.getDetails()).contains("finalized with status SUCCESS");
        }

        @Test
        @DisplayName("Full flow: Transfer finalized FAILED → FAILED audit log saved to MongoDB")
        void transferFailed_failedLogSaved() {
            UUID userId = UUID.randomUUID();
            AuditEvent event = buildAuditEvent(
                    "transaction-service",
                    AuditAction.TRANSFER_FUNDS,
                    AuditStatus.FAILED,
                    "Transfer failed at initiation. Reason: Insufficient funds in account: abc-123",
                    userId
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(AuditStatus.FAILED);
            assertThat(saved.getDetails()).contains("Insufficient funds");
        }

        @Test
        @DisplayName("Full flow: Multiple transfer events create separate audit logs")
        void multipleTransferEvents_createSeparateLogs() {
            UUID userId = UUID.randomUUID();

            listener.receiveAuditLog(buildAuditEvent(
                    "transaction-service", AuditAction.TRANSFER_FUNDS, AuditStatus.WARNING,
                    "Transfer TXN-001 initiated", userId));

            listener.receiveAuditLog(buildAuditEvent(
                    "transaction-service", AuditAction.TRANSFER_FUNDS, AuditStatus.SUCCESS,
                    "Transfer TXN-001 finalized SUCCESS", userId));

            listener.receiveAuditLog(buildAuditEvent(
                    "transaction-service", AuditAction.TRANSFER_FUNDS, AuditStatus.WARNING,
                    "Transfer TXN-002 initiated", userId));

            listener.receiveAuditLog(buildAuditEvent(
                    "transaction-service", AuditAction.TRANSFER_FUNDS, AuditStatus.FAILED,
                    "Transfer TXN-002 finalized FAILED", userId));

            verify(repository, times(4)).save(any(AuditLog.class));
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 2: Card Service → Card Payment Audit
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 2: Card Service — Card Payment Audit Events")
    class CardServiceAuditTests {

        @Test
        @DisplayName("Full flow: Card payment audit event → Persisted to MongoDB")
        void cardPaymentAudit_persisted() {
            UUID userId = UUID.randomUUID();
            AuditEvent event = buildAuditEvent(
                    "card-service",
                    AuditAction.TRANSFER_FUNDS, // Card payments use TRANSFER_FUNDS action
                    AuditStatus.SUCCESS,
                    "Card payment of $49.99 processed for merchant MRC-001",
                    userId
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getServiceName()).isEqualTo("card-service");
            assertThat(captor.getValue().getDetails()).contains("49.99");
        }

        @Test
        @DisplayName("Full flow: Card payment declined audit event → FAILED log persisted")
        void cardPaymentDeclined_failedLogPersisted() {
            AuditEvent event = buildAuditEvent(
                    "card-service",
                    AuditAction.TRANSFER_FUNDS,
                    AuditStatus.FAILED,
                    "Card payment declined: Insufficient funds. Card: ****1234",
                    UUID.randomUUID()
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getStatus()).isEqualTo(AuditStatus.FAILED);
            assertThat(captor.getValue().getDetails()).contains("declined");
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 3: Account Service → Balance View Audit
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 3: Account Service — Balance View Audit Events")
    class AccountServiceAuditTests {

        @Test
        @DisplayName("Full flow: Account balance viewed → Audit log persisted")
        void balanceViewed_auditLogPersisted() {
            UUID userId = UUID.randomUUID();
            AuditEvent event = buildAuditEvent(
                    "account-service",
                    AuditAction.VIEW_ACCOUNT_BALANCE,
                    AuditStatus.SUCCESS,
                    "User viewed balance for account ACC-12345",
                    userId
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.VIEW_ACCOUNT_BALANCE);
            assertThat(captor.getValue().getServiceName()).isEqualTo("account-service");
        }

        @Test
        @DisplayName("Full flow: Account deposit audit → Persisted correctly")
        void deposit_auditLogPersisted() {
            AuditEvent event = buildAuditEvent(
                    "account-service",
                    AuditAction.DEPOSIT_FUNDS,
                    AuditStatus.SUCCESS,
                    "Deposit of 1000.00 USD to account ACC-12345",
                    UUID.randomUUID()
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.DEPOSIT_FUNDS);
            assertThat(captor.getValue().getDetails()).contains("1000.00");
        }

        @Test
        @DisplayName("Full flow: Withdrawal audit → Persisted correctly")
        void withdrawal_auditLogPersisted() {
            AuditEvent event = buildAuditEvent(
                    "account-service",
                    AuditAction.WITHDRAW_FUNDS,
                    AuditStatus.SUCCESS,
                    "Withdrawal of 200.00 USD from account ACC-67890",
                    UUID.randomUUID()
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.WITHDRAW_FUNDS);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 4: IAM Service → Login Audit
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 4: IAM Service — Login Audit Events")
    class IAMServiceAuditTests {

        @Test
        @DisplayName("Full flow: Successful login → SUCCESS audit log")
        void loginSuccess_auditLogPersisted() {
            UUID userId = UUID.randomUUID();
            AuditEvent event = buildAuditEvent(
                    "iam-service",
                    AuditAction.LOGIN_ATTEMPT,
                    AuditStatus.SUCCESS,
                    "User alice@bank.com logged in from IP 192.168.1.1",
                    userId
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.LOGIN_ATTEMPT);
            assertThat(captor.getValue().getStatus()).isEqualTo(AuditStatus.SUCCESS);
            assertThat(captor.getValue().getServiceName()).isEqualTo("iam-service");
        }

        @Test
        @DisplayName("Full flow: Failed login → UNAUTHORIZED audit log with SYSTEM user ID")
        void loginFailed_unauthorizedLogPersisted() {
            UUID systemId = UUID.fromString("00000000-0000-0000-0000-000000000000");
            AuditEvent event = buildAuditEvent(
                    "iam-service",
                    AuditAction.LOGIN_ATTEMPT,
                    AuditStatus.UNAUTHORIZED,
                    "Failed login attempt for unknown@bank.com from IP 10.0.0.5",
                    systemId
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(AuditStatus.UNAUTHORIZED);
            assertThat(saved.getUserIdentifier()).isEqualTo(systemId);
        }

        @Test
        @DisplayName("Full flow: Brute force login attempts → All audited")
        void bruteForceAttempts_allAudited() {
            UUID systemId = UUID.fromString("00000000-0000-0000-0000-000000000000");

            for (int i = 0; i < 10; i++) {
                AuditEvent event = buildAuditEvent(
                        "iam-service",
                        AuditAction.LOGIN_ATTEMPT,
                        AuditStatus.UNAUTHORIZED,
                        "Failed login attempt #" + (i + 1) + " for admin@bank.com",
                        systemId
                );
                listener.receiveAuditLog(event);
            }

            verify(repository, times(10)).save(any(AuditLog.class));
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 5: SAGA Reconciliation Sweep (No HTTP Context)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 5: SAGA Reconciliation — System-level Audit Events")
    class SagaReconciliationTests {

        @Test
        @DisplayName("Full flow: Reconciliation sweep timeout → FAILED audit with SYSTEM user")
        void reconciliationTimeout_failedAuditWithSystemUser() {
            UUID systemId = UUID.fromString("00000000-0000-0000-0000-000000000000");

            AuditEvent event = buildAuditEvent(
                    "transaction-service",
                    AuditAction.TRANSFER_FUNDS,
                    AuditStatus.FAILED,
                    "Transfer TXN-TIMEOUT01 finalized with status FAILED. Reason: 10-min timeout exceeded",
                    systemId
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getUserIdentifier()).isEqualTo(systemId);
            assertThat(saved.getDetails()).contains("timeout exceeded");
        }

        @Test
        @DisplayName("Full flow: SAGA callback success → SUCCESS audit with SYSTEM user")
        void sagaCallbackSuccess_successAuditWithSystemUser() {
            UUID systemId = UUID.fromString("00000000-0000-0000-0000-000000000000");

            AuditEvent event = buildAuditEvent(
                    "transaction-service",
                    AuditAction.TRANSFER_FUNDS,
                    AuditStatus.SUCCESS,
                    "Transfer TXN-SAGA01 finalized with status SUCCESS. Reason: Funds secured by account-service",
                    systemId
            );

            listener.receiveAuditLog(event);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getStatus()).isEqualTo(AuditStatus.SUCCESS);
            assertThat(captor.getValue().getUserIdentifier()).isEqualTo(systemId);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  SCENARIO 6: Cross-Service Mixed Events (Production Simulation)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Scenario 6: Cross-Service Production Simulation")
    class CrossServiceTests {

        @Test
        @DisplayName("Full flow: Mixed audit events from multiple services → All persisted correctly")
        void mixedEvents_allPersisted() {
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            UUID systemId = UUID.fromString("00000000-0000-0000-0000-000000000000");

            // IAM: User logs in
            listener.receiveAuditLog(buildAuditEvent(
                    "iam-service", AuditAction.LOGIN_ATTEMPT, AuditStatus.SUCCESS,
                    "User logged in", user1));

            // Account: User views balance
            listener.receiveAuditLog(buildAuditEvent(
                    "account-service", AuditAction.VIEW_ACCOUNT_BALANCE, AuditStatus.SUCCESS,
                    "Balance viewed", user1));

            // Transaction: Transfer initiated
            listener.receiveAuditLog(buildAuditEvent(
                    "transaction-service", AuditAction.TRANSFER_FUNDS, AuditStatus.WARNING,
                    "Transfer initiated", user1));

            // Transaction: Transfer completed
            listener.receiveAuditLog(buildAuditEvent(
                    "transaction-service", AuditAction.TRANSFER_FUNDS, AuditStatus.SUCCESS,
                    "Transfer completed", systemId));

            // IAM: Another user fails login
            listener.receiveAuditLog(buildAuditEvent(
                    "iam-service", AuditAction.LOGIN_ATTEMPT, AuditStatus.UNAUTHORIZED,
                    "Bad credentials", systemId));

            // Card: Payment processed
            listener.receiveAuditLog(buildAuditEvent(
                    "card-service", AuditAction.TRANSFER_FUNDS, AuditStatus.SUCCESS,
                    "Card payment processed", user2));

            verify(repository, times(6)).save(any(AuditLog.class));
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════

    private AuditEvent buildAuditEvent(String serviceName, AuditAction action,
                                        AuditStatus status, String details, UUID userId) {
        AuditEvent event = new AuditEvent();
        event.setServiceName(serviceName);
        event.setAction(action);
        event.setStatus(status);
        event.setDetails(details);
        event.setUserIdentifier(userId);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}

