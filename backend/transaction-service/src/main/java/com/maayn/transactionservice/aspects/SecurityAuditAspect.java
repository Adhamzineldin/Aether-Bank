package com.maayn.transactionservice.aspects;

import com.maayn.transactionservice.config.RabbitMQConfig;
import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import com.maayn.transactionservice.utils.UserContextResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.transaction.TransactionResponse;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.audit.models.shared.AuditAction;
import maayn.veld.generated.sdk.audit.models.shared.AuditEvent;
import maayn.veld.generated.sdk.audit.models.shared.AuditStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditAspect {

    private final RabbitTemplate rabbitTemplate;
    private final TransferIdempotencyHandler idempotencyHandler;
    private final UserContextResolver userContextResolver;

    @Value("${spring.application.name}")
    private String serviceName;

    @Around("execution(* com.maayn.transactionservice.service.TransactionService.transfer(..)) && args(request)")
    public Object aroundTransfer(ProceedingJoinPoint pjp, TransferRequest request) throws Throwable {

        boolean isNewRequest = idempotencyHandler.getIfAlreadyProcessed(request.getIdempotencyKey()).isEmpty();

        try {
            Object result = pjp.proceed();

            if (isNewRequest) {
                auditSuccess(request, (TransactionResponse) result);
            }
            return result;

        } catch (Exception e) {
            if (isNewRequest) {
                auditFailure(request, e);
            }
            throw e;
        }
    }

    // --- PRIVATE AUDIT HELPERS ---

    private void auditSuccess(TransferRequest request, TransactionResponse response) {
        String details = String.format("Transfer %s completed successfully. Amount: %s",
                response.getReferenceNumber(), request.getAmount());

        publishEvent(AuditAction.TRANSFER_FUNDS, AuditStatus.SUCCESS, details, request);
    }

    private void auditFailure(TransferRequest request, Exception e) {
        String details = String.format("Transfer failed at core engine. Key: %s. Reason: %s",
                request.getIdempotencyKey(), e.getMessage());

        publishEvent(AuditAction.TRANSFER_FUNDS, AuditStatus.FAILED, details, request);
    }

    private void publishEvent(AuditAction action, AuditStatus status, String details, TransferRequest request) {
        UUID userId = userContextResolver.resolveUserId(request);

        AuditEvent event = new AuditEvent(
                this.serviceName, action, status, details, userId, LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.AUDIT_EXCHANGE, RabbitMQConfig.AUDIT_ROUTING_KEY, event);
    }
}