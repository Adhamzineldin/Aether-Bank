package com.maayn.transactionservice.aspects;

import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import maayn.veld.generated.sdk.iam.constants.AccountSystemConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.audit.models.shared.*;
import maayn.veld.generated.models.transaction.TransactionResponse;
import maayn.veld.generated.models.transaction.TransferRequest;
import com.maayn.transactionservice.config.RabbitMQConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditAspect {

    private final RabbitTemplate rabbitTemplate;
    private final TransferIdempotencyHandler idempotencyHandler;

    @Value("${spring.application.name}")
    private String serviceName;

    /**
     * Wraps the atomic transfer() method to:
     * 1. Detect idempotent replays and skip auditing them (prevents audit spam).
     * 2. Audit successful execution instantly as SUCCESS.
     * 3. Audit immediate failures (like insufficient funds) as FAILED.
     */
    @Around("execution(* com.maayn.transactionservice.service.TransactionService.transfer(..)) && args(request)")
    public Object aroundTransfer(ProceedingJoinPoint pjp, TransferRequest request) throws Throwable {

        // 1. Check if this is a double-click
        boolean isIdempotentReplay =
                idempotencyHandler.getIfAlreadyProcessed(request.getIdempotencyKey()).isPresent();

        try {
            // 2. Proceed with the actual Ledger math
            Object result = pjp.proceed();

            // 3. Audit fresh successes
            if (!isIdempotentReplay) {
                TransactionResponse response = (TransactionResponse) result;
                String details = String.format(
                        "Transfer %s completed successfully. Amount: %s",
                        response.getReferenceNumber(), request.getAmount());

                sendAuditMessage(AuditAction.TRANSFER_FUNDS, AuditStatus.SUCCESS, details, extractUserIdFromContext());
            }

            return result;

        } catch (Exception e) {
            // 4. Audit fresh failures (e.g. Insufficient Funds thrown by LedgerService)
            if (!isIdempotentReplay) {
                String details = String.format("Transfer failed at core engine. Key: %s. Reason: %s",
                        request.getIdempotencyKey(), e.getMessage());

                sendAuditMessage(AuditAction.TRANSFER_FUNDS, AuditStatus.FAILED, details, extractUserIdFromContext());
            }
            throw e; // Rethrow so the global exception handler or SAGA listener can catch it
        }
    }

    // --- PRIVATE HELPERS ---

    private void sendAuditMessage(AuditAction action, AuditStatus status, String details, UUID userId) {
        AuditEvent event = new AuditEvent(
                this.serviceName,
                action,
                status,
                details,
                userId,
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.AUDIT_EXCHANGE, RabbitMQConfig.AUDIT_ROUTING_KEY, event);
    }

    private UUID extractUserIdFromContext() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest httpRequest = attributes.getRequest();

                String userIdHeader = httpRequest.getHeader("X-User-Id");

                if (userIdHeader != null && !userIdHeader.isBlank()) {
                    return UUID.fromString(userIdHeader);
                }
            }
        } catch (Exception e) {
            log.debug("No HTTP Context available (likely a background SAGA command). Falling back to SYSTEM_USER.");
        }

        return AccountSystemConfig.SYSTEM_USER_ID;
    }
}