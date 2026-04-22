package com.maayn.auditservice.listener;

import com.maayn.auditservice.config.RabbitMQConfig;
import com.maayn.auditservice.entity.AuditLog;
import com.maayn.auditservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditListener {

    private final AuditLogRepository repository;

    /**
     * Consumes audit events as a raw {@code Map<String,Object>} rather than the
     * generated {@code AuditEvent} typed model. The veld {@code AuditAction}
     * enum is a closed set ({@code TRANSFER_FUNDS}, {@code DEPOSIT_FUNDS},
     * {@code WITHDRAW_FUNDS}, {@code VIEW_ACCOUNT_BALANCE}, {@code LOGIN_ATTEMPT})
     * whose {@code @JsonCreator} throws on unknown values — so every event
     * other services emit ({@code OPEN_ACCOUNT}, {@code ISSUE_CARD},
     * {@code DISBURSE_LOAN}, {@code PROCESS_MERCHANT_PAYMENT},
     * {@code NOTIFICATION_EMAIL_SENT}, {@code LOGIN_SUCCESS}, …) was being
     * NACKed at deserialization, which is why the admin dashboard only ever
     * showed transaction-service activity. Keeping the wire shape free-form
     * lets every service publish whatever action verb it needs without an
     * SDK regen.
     */
    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void receiveAuditLog(Map<String, Object> event) {
        AuditLog logEntry = new AuditLog();
        logEntry.setServiceName(asString(event.get("serviceName")));
        logEntry.setAction(asString(event.get("action")));
        logEntry.setStatus(asString(event.get("status")));
        logEntry.setDetails(asString(event.get("details")));
        logEntry.setUserIdentifier(asUuid(event.get("userIdentifier")));
        logEntry.setTimestamp(asTimestamp(event.get("timestamp")));

        log.info("SECURITY AUDIT CAUGHT: [{}] action '{}' from {}",
                logEntry.getStatus(), logEntry.getAction(), logEntry.getServiceName());

        repository.save(logEntry);
        log.debug("Audit event persisted: {}", logEntry.getId());
    }

    private static String asString(Object v) {
        return v == null ? null : v.toString();
    }

    private static UUID asUuid(Object v) {
        if (v == null) return null;
        try {
            return v instanceof UUID u ? u : UUID.fromString(v.toString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static LocalDateTime asTimestamp(Object v) {
        if (v == null) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(v.toString());
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }
}