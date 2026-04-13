package com.maayn.auditservice.entity;

import lombok.Data;

import maayn.veld.generated.models.shared.AuditAction;
import maayn.veld.generated.models.shared.AuditStatus;
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
    private AuditAction action;
    private AuditStatus status; 
    private String details;
    private UUID userIdentifier;
    private LocalDateTime timestamp;
}