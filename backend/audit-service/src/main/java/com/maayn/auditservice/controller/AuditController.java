package com.maayn.auditservice.controller;

import com.maayn.auditservice.entity.AuditLog;
import com.maayn.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit/audit/logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<AuditLog> logs = auditService.getAuditLogs(
            serviceName, action, userId, startDate, endDate, page, pageSize
        );

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLog(@PathVariable String id) {
        AuditLog log = auditService.getAuditLog(id);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLog>> getAuditsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<AuditLog> logs = auditService.getAuditsByUser(userId, page, pageSize);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<Page<AuditLog>> getAuditsByService(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<AuditLog> logs = auditService.getAuditsByService(serviceName, page, pageSize);
        return ResponseEntity.ok(logs);
    }
}

