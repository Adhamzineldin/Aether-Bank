package com.maayn.financialservice.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lightweight audit-event publisher for financial-service. See
 * {@code com.maayn.cardservice.audit.AuditPublisher} for the full rationale —
 * same shape, decoupled from any generated SDK so it works without
 * regenerating the veld bundle.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditPublisher {

    public static final String AUDIT_EXCHANGE = "security_audit_exchange";
    public static final String AUDIT_ROUTING_KEY = "audit.log";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.application.name:financial-service}")
    private String serviceName;

    public void publishSuccess(String action, UUID userId, String details) {
        publish(action, STATUS_SUCCESS, userId, details);
    }

    public void publishFailure(String action, UUID userId, String details) {
        publish(action, STATUS_FAILED, userId, details);
    }

    public void publish(String action, String status, UUID userId, String details) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("serviceName", serviceName);
            event.put("action", action);
            event.put("status", status);
            event.put("details", details);
            event.put("userIdentifier", userId != null ? userId.toString() : null);
            event.put("timestamp", LocalDateTime.now().toString());

            rabbitTemplate.convertAndSend(AUDIT_EXCHANGE, AUDIT_ROUTING_KEY, event);
        } catch (Exception ex) {
            log.warn("Failed to publish audit event action={} status={}: {}", action, status, ex.getMessage());
        }
    }
}

