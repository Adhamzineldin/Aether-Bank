package com.maayn.transactionservice.aspects;

import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import com.maayn.transactionservice.utils.UserContextResolver;
import maayn.veld.generated.errors.TransferException;
import maayn.veld.generated.models.transaction.TransactionResponse;
import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.audit.models.shared.AuditAction;
import maayn.veld.generated.sdk.audit.models.shared.AuditEvent;
import maayn.veld.generated.sdk.audit.models.shared.AuditStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityAuditAspect Unit Tests")
class SecurityAuditAspectTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private TransferIdempotencyHandler idempotencyHandler;
    @Mock private UserContextResolver userContextResolver;
    @Mock private ProceedingJoinPoint joinPoint;

    @InjectMocks private SecurityAuditAspect aspect;

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private TransferRequest validRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aspect, "serviceName", "transaction-service");

        // Default: no HTTP context → falls back to SYSTEM_USER_ID
        lenient().when(userContextResolver.resolveUserId(any(TransferRequest.class)))
                .thenReturn(SYSTEM_USER_ID);

        validRequest = new TransferRequest();
        validRequest.setIdempotencyKey("idem-aspect-001");
        validRequest.setSourceAccountId(UUID.randomUUID());
        validRequest.setDestinationAccountId(UUID.randomUUID());
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setCurrency("USD");
        validRequest.setType(TransactionType.TRANSFER);
    }

    // ════════════════════════════════════════════════════════════════
    //  aroundTransfer() — Fresh Transfer (not idempotent replay)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("aroundTransfer() — Fresh Transfers")
    class FreshTransferTests {

        @Test @DisplayName("Should send SUCCESS audit event for fresh successful transfer")
        void aroundTransfer_freshSuccess_sendsSuccessAudit() throws Throwable {
            when(idempotencyHandler.getIfAlreadyProcessed("idem-aspect-001"))
                    .thenReturn(Optional.empty());

            TransactionResponse response = new TransactionResponse();
            response.setReferenceNumber("TXN-AUDIT01");
            response.setAmount(new BigDecimal("100.00"));
            response.setStatus(TransactionStatus.SUCCESS);
            response.setTimestamp(LocalDateTime.now());

            when(joinPoint.proceed()).thenReturn(response);

            Object result = aspect.aroundTransfer(joinPoint, validRequest);

            assertThat(result).isEqualTo(response);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), captor.capture());

            AuditEvent auditEvent = captor.getValue();
            assertThat(auditEvent.getServiceName()).isEqualTo("transaction-service");
            assertThat(auditEvent.getAction()).isEqualTo(AuditAction.TRANSFER_FUNDS);
            assertThat(auditEvent.getStatus()).isEqualTo(AuditStatus.SUCCESS);
            assertThat(auditEvent.getDetails()).contains("TXN-AUDIT01");
            assertThat(auditEvent.getDetails()).contains("completed successfully");
        }

        @Test
        @DisplayName("Should send FAILED audit event when transfer throws exception")
        void aroundTransfer_freshFailure_sendsFailedAudit() throws Throwable {
            when(idempotencyHandler.getIfAlreadyProcessed("idem-aspect-001"))
                    .thenReturn(Optional.empty());

            when(joinPoint.proceed()).thenThrow(
                    TransferException.insufficientFunds("Insufficient funds in account"));

            assertThatThrownBy(() -> aspect.aroundTransfer(joinPoint, validRequest))
                    .isInstanceOf(TransferException.class);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), captor.capture());

            AuditEvent auditEvent = captor.getValue();
            assertThat(auditEvent.getStatus()).isEqualTo(AuditStatus.FAILED);
            assertThat(auditEvent.getDetails()).contains("Transfer failed at core engine");
            assertThat(auditEvent.getDetails()).contains("Insufficient funds");
        }

        @Test
        @DisplayName("Should re-throw the original exception after auditing")
        void aroundTransfer_freshFailure_rethrowsException() throws Throwable {
            when(idempotencyHandler.getIfAlreadyProcessed("idem-aspect-001"))
                    .thenReturn(Optional.empty());

            TransferException ex = TransferException.invalidAmount("Bad amount");
            when(joinPoint.proceed()).thenThrow(ex);

            assertThatThrownBy(() -> aspect.aroundTransfer(joinPoint, validRequest))
                    .isSameAs(ex);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  aroundTransfer() — Idempotent Replay
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("aroundTransfer() — Idempotent Replays")
    class IdempotentReplayTests {

        @Test
        @DisplayName("Should NOT send audit event for idempotent replay (success)")
        void aroundTransfer_replay_noAuditSent() throws Throwable {
            TransactionResponse cached = new TransactionResponse();
            cached.setReferenceNumber("TXN-CACHED");
            cached.setStatus(TransactionStatus.SUCCESS);

            when(idempotencyHandler.getIfAlreadyProcessed("idem-aspect-001"))
                    .thenReturn(Optional.of(cached));

            when(joinPoint.proceed()).thenReturn(cached);

            Object result = aspect.aroundTransfer(joinPoint, validRequest);

            assertThat(result).isEqualTo(cached);
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(AuditEvent.class));
        }

        @Test
        @DisplayName("Should NOT send audit event for idempotent replay even if exception occurs")
        void aroundTransfer_replayException_noAuditSent() throws Throwable {
            TransactionResponse cached = new TransactionResponse();
            cached.setReferenceNumber("TXN-CACHED");

            when(idempotencyHandler.getIfAlreadyProcessed("idem-aspect-001"))
                    .thenReturn(Optional.of(cached));

            when(joinPoint.proceed()).thenThrow(new RuntimeException("Unexpected"));

            assertThatThrownBy(() -> aspect.aroundTransfer(joinPoint, validRequest))
                    .isInstanceOf(RuntimeException.class);

            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(AuditEvent.class));
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Audit Event Properties
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Audit Event Properties")
    class AuditEventPropertyTests {

        @Test
        @DisplayName("Should set timestamp on audit event")
        void aroundTransfer_setsTimestamp() throws Throwable {
            when(idempotencyHandler.getIfAlreadyProcessed("idem-aspect-001"))
                    .thenReturn(Optional.empty());

            TransactionResponse response = new TransactionResponse();
            response.setReferenceNumber("TXN-TS");
            response.setAmount(new BigDecimal("50.00"));
            response.setStatus(TransactionStatus.SUCCESS);
            response.setTimestamp(LocalDateTime.now());
            when(joinPoint.proceed()).thenReturn(response);

            aspect.aroundTransfer(joinPoint, validRequest);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), captor.capture());

            assertThat(captor.getValue().getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should set userIdentifier (falls back to SYSTEM when no HTTP context)")
        void aroundTransfer_fallsBackToSystemUser() throws Throwable {
            when(idempotencyHandler.getIfAlreadyProcessed("idem-aspect-001"))
                    .thenReturn(Optional.empty());

            TransactionResponse response = new TransactionResponse();
            response.setReferenceNumber("TXN-SYS");
            response.setAmount(new BigDecimal("10.00"));
            response.setStatus(TransactionStatus.SUCCESS);
            response.setTimestamp(LocalDateTime.now());
            when(joinPoint.proceed()).thenReturn(response);

            aspect.aroundTransfer(joinPoint, validRequest);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), captor.capture());

            // Without HTTP context, should fall back to SYSTEM_USER_ID (all zeros)
            assertThat(captor.getValue().getUserIdentifier()).isNotNull();
            assertThat(captor.getValue().getUserIdentifier().toString()).isEqualTo("00000000-0000-0000-0000-000000000000");
        }

        @Test
        @DisplayName("Should include amount in audit details")
        void aroundTransfer_includesAmountInDetails() throws Throwable {
            when(idempotencyHandler.getIfAlreadyProcessed("idem-aspect-001"))
                    .thenReturn(Optional.empty());

            TransactionResponse response = new TransactionResponse();
            response.setReferenceNumber("TXN-AMT");
            response.setAmount(new BigDecimal("100.00"));
            response.setStatus(TransactionStatus.SUCCESS);
            response.setTimestamp(LocalDateTime.now());
            when(joinPoint.proceed()).thenReturn(response);

            aspect.aroundTransfer(joinPoint, validRequest);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), captor.capture());

            assertThat(captor.getValue().getDetails()).contains("100.00");
        }
    }
}

