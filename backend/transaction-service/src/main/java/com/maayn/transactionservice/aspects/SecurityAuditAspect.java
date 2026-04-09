package com.maayn.transactionservice.aspects;

import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import maayn.veld.generated.sdk.iam.constants.AccountSystemConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.audit.models.shared.*;
import maayn.veld.generated.models.TransactionResponse;
import maayn.veld.generated.models.TransactionStatus;
import maayn.veld.generated.models.TransferRequest;
import com.maayn.transactionservice.config.RabbitMQConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
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
     * Wraps transfer() to:
     *   1. Detect idempotent replays before proceeding (single extra key-lookup).
     *   2. Audit fresh initiations as WARNING ("initiated / PENDING").
     *   3. Audit immediate validation failures as FAILED.
     *   4. Skip audit entirely for replays — the original initiation was already recorded.
     */
    @Around("execution(* com.maayn.transactionservice.service.TransactionService.transfer(..)) && args(request)")
    public Object aroundTransfer(ProceedingJoinPoint pjp, TransferRequest request) throws Throwable {

        boolean isIdempotentReplay =
                idempotencyHandler.getIfAlreadyProcessed(request.getIdempotencyKey()).isPresent();

        try {
            Object result = pjp.proceed();

            if (!isIdempotentReplay) {
                TransactionResponse response = (TransactionResponse) result;
                String details = String.format(
                        "Transfer %s initiated. Amount: %s",
                        response.getReferenceNumber(), request.getAmount());
                sendAuditMessage(AuditAction.TRANSFER_FUNDS, AuditStatus.WARNING, details, extractUserIdFromContext());
            }

            return result;

        } catch (Exception e) {
            if (!isIdempotentReplay) {
                String details = String.format("Transfer failed at initiation. Reason: %s", e.getMessage());
                sendAuditMessage(AuditAction.TRANSFER_FUNDS, AuditStatus.FAILED, details, extractUserIdFromContext());
            }
            throw e;
        }
    }


    /**
     * Authoritative audit for every finalized transaction.
     * Fires for all three callers:
     *   - TransferSagaResponseListener (SAGA success / failure callbacks)
     *   - TransactionReconciliationSweeper (10-min timeout rollbacks)
     *
     * No HTTP context exists for the latter two callers; extractUserIdFromContext()
     * falls back to SYSTEM_USER_ID automatically.
     */
    @AfterReturning(
            pointcut = "execution(* com.maayn.transactionservice.service.TransactionService.finalizeTransaction(..)) && args(referenceNumber, finalStatus, reason)",
            argNames = "referenceNumber,finalStatus,reason"
    )
    public void auditFinalizedTransaction(String referenceNumber,
                                          TransactionStatus finalStatus,
                                          String reason) {

        AuditStatus auditStatus = (finalStatus == TransactionStatus.SUCCESS)
                ? AuditStatus.SUCCESS
                : AuditStatus.FAILED;

        String details = String.format(
                "Transfer %s finalized with status %s. Reason: %s",
                referenceNumber, finalStatus, reason);

        sendAuditMessage(AuditAction.TRANSFER_FUNDS, auditStatus, details, extractUserIdFromContext());
    }


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
            log.warn("Could not extract identity from HTTP Context. Falling back to SYSTEM.");
        }

        return AccountSystemConfig.SYSTEM_USER_ID;
    }
}


