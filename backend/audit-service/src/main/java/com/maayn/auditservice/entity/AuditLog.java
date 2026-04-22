package com.maayn.auditservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Document(collection = "security_logs")
public class AuditLog {
    
    @Id
    private String id; 
    
    private String serviceName;
    // Stored as free-form strings (not the closed-set veld AuditAction/AuditStatus
    // enums) so any service can publish its own action verbs without having to
    // round-trip a SDK regen — e.g. ISSUE_CARD, OPEN_ACCOUNT, DISBURSE_LOAN,
    // PROCESS_MERCHANT_PAYMENT, NOTIFICATION_EMAIL_SENT, LOGIN_SUCCESS.
    private String action;
    private String status; 
    private String details;
    private UUID userIdentifier;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}