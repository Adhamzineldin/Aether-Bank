package com.maayn.iamservice.service;

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
 * Logs all security-relevant events for compliance and debugging
 */
@Slf4j
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
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
