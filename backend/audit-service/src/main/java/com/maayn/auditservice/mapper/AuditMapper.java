package com.maayn.auditservice.mapper;

import com.maayn.auditservice.entity.AuditLog;
import maayn.veld.generated.models.AuditEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditMapper {

    public static AuditLog toEntity(AuditEvent event) {
        AuditLog logEntry = new AuditLog();
        logEntry.setServiceName(event.getServiceName());
        logEntry.setAction(event.getAction());
        logEntry.setStatus(event.getStatus());
        logEntry.setDetails(event.getDetails());
        logEntry.setUserIdentifier(event.getUserIdentifier());
        logEntry.setTimestamp(event.getTimestamp());
        
        return logEntry;
    }
}