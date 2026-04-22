package com.maayn.auditservice.mapper;

import com.maayn.auditservice.entity.AuditLog;
import maayn.veld.generated.models.shared.AuditEvent;

/**
 * Legacy mapper from the typed {@link AuditEvent} to the persisted
 * {@link AuditLog}. The hot listener path no longer uses this — it deserialises
 * directly from a {@code Map<String,Object>} so unknown action verbs from
 * other services don't blow up the closed enum {@code @JsonCreator}. Kept for
 * unit-test coverage and for any code path that still has a typed event in
 * hand.
 */
public class AuditMapper {

    public static AuditLog toEntity(AuditEvent event) {
        AuditLog logEntry = new AuditLog();
        logEntry.setServiceName(event.getServiceName());
        logEntry.setAction(event.getAction() != null ? event.getAction().name() : null);
        logEntry.setStatus(event.getStatus() != null ? event.getStatus().name() : null);
        logEntry.setDetails(event.getDetails());
        logEntry.setUserIdentifier(event.getUserIdentifier());
        logEntry.setTimestamp(event.getTimestamp());
        return logEntry;
    }
}