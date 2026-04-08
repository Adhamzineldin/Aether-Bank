package com.maayn.transactionservice.aspects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.audit.models.shared.*;
import maayn.veld.generated.models.TransactionResponse;
import maayn.veld.generated.models.TransferRequest;
import com.maayn.transactionservice.config.RabbitMQConfig;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditAspect {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.application.name}")
    private String serviceName; // Grabs "transaction-service" safely

    @AfterReturning(
            pointcut = "execution(* com.maayn.transactionservice.service.TransactionService.transfer(..)) && args(request)",
            returning = "response"
    )
    public void auditSuccessfulTransfer(JoinPoint joinPoint, TransferRequest request, TransactionResponse response) {
        String details = String.format("Transfer %s successful. Amount: %s", response.getReferenceNumber(), request.getAmount());
        sendAuditMessage(AuditAction.TRANSFER_FUNDS, AuditStatus.SUCCESS, details);
    }

    @AfterThrowing(
            pointcut = "execution(* com.maayn.transactionservice.service.TransactionService.transfer(..)) && args(request)",
            throwing = "exception"
    )
    public void auditFailedTransfer(JoinPoint joinPoint, TransferRequest request, Exception exception) {
        String details = String.format("Transfer failed. Reason: %s", exception.getMessage());
        sendAuditMessage(AuditAction.TRANSFER_FUNDS, AuditStatus.FAILED, details);
    }

    private void sendAuditMessage(AuditAction action, AuditStatus status, String details) {
        AuditEvent event = new AuditEvent(
                this.serviceName,
                action,
                status,
                details,
                "SYSTEM",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.AUDIT_EXCHANGE, RabbitMQConfig.AUDIT_ROUTING_KEY, event);
        log.debug("Sent audit log to RabbitMQ");
    }
}