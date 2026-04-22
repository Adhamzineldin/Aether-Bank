package com.maayn.iamservice.service;

import com.maayn.iamservice.audit.AuditPublisher;
import com.maayn.iamservice.domain.entity.AuditLog;
import com.maayn.iamservice.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Audit Service
 * Logs all security-relevant events for compliance and debugging.
 *
 * <p>Dual-writes:
 * <ul>
 *   <li>Local PostgreSQL {@code audit_log} table — kept for IAM-internal
 *       compliance (IP / user-agent / request-path forensics).</li>
 *   <li>Central {@code security_audit_exchange} on RabbitMQ via
 *       {@link AuditPublisher} — feeds the audit-service so the admin
 *       dashboard sees the same event stream as every other microservice.</li>
 * </ul>
 */
@Slf4j
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditPublisher auditPublisher;

    public AuditService(AuditLogRepository auditLogRepository, AuditPublisher auditPublisher) {
        this.auditLogRepository = auditLogRepository;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Log audit event with user context
     */
    @Transactional
    public void logAuditEvent(String action, String entityType, String entityId, 
                              String oldValue, String newValue, String details) {
        try {
            HttpServletRequest request = getHttpServletRequest();
            
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .status("SUCCESS")
                    .ipAddress(request != null ? getClientIp(request) : null)
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .requestMethod(request != null ? request.getMethod() : null)
                    .requestPath(request != null ? request.getRequestURI() : null)
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Audit event logged: {}", action);
        } catch (Exception e) {
            log.error("Failed to log audit event: {}", action, e);
        }
        // Mirror to central audit-service (best-effort, non-fatal).
        auditPublisher.publishSuccess(action,
                null,
                buildDetails(entityType, entityId, oldValue, newValue, details));
    }

    /**
     * Log login attempt
     */
    @Transactional
    public void logLoginAttempt(UUID userId, boolean success, String details) {
        try {
            HttpServletRequest request = getHttpServletRequest();
            
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(success ? "LOGIN_SUCCESS" : "LOGIN_FAILURE")
                    .entityType("USER")
                    .status(success ? "SUCCESS" : "FAILURE")
                    .statusCode(success ? 200 : 401)
                    .newValue(details)
                    .ipAddress(request != null ? getClientIp(request) : null)
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .requestMethod(request != null ? request.getMethod() : null)
                    .requestPath(request != null ? request.getRequestURI() : null)
                    .build();
            
            auditLogRepository.save(auditLog);
            log.info("Login attempt logged for user: {}, Success: {}", userId, success);
        } catch (Exception e) {
            log.error("Failed to log login attempt", e);
        }
        // Mirror to central audit-service.
        if (success) {
            auditPublisher.publishSuccess("LOGIN_SUCCESS", userId, details);
        } else {
            auditPublisher.publishFailure("LOGIN_FAILURE", userId, details);
        }
    }

    private String buildDetails(String entityType, String entityId, String oldValue, String newValue, String details) {
        StringBuilder sb = new StringBuilder();
        if (entityType != null) sb.append("entityType=").append(entityType).append(' ');
        if (entityId != null) sb.append("entityId=").append(entityId).append(' ');
        if (oldValue != null) sb.append("oldValue=").append(oldValue).append(' ');
        if (newValue != null) sb.append("newValue=").append(newValue).append(' ');
        if (details != null) sb.append(details);
        return sb.toString().trim();
    }

    /**
     * Log token refresh
     */
    @Transactional
    public void logTokenRefresh(UUID userId) {
        logAuditEvent("TOKEN_REFRESHED", "TOKEN", null, null, null, "Token refreshed for user");
    }

    /**
     * Extract client IP from request
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    /**
     * Get current HTTP request
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
