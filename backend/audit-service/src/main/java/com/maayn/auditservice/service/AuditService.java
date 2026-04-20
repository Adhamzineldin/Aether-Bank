package com.maayn.auditservice.service;

import com.maayn.auditservice.entity.AuditLog;
import com.maayn.auditservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final MongoTemplate mongoTemplate;

    public Page<AuditLog> getAuditLogs(String serviceName, String action, UUID userId, 
                                        LocalDateTime startDate, LocalDateTime endDate,
                                        int page, int pageSize) {
        log.info("Fetching audit logs with filters");

        Criteria criteria = new Criteria();
        List<Criteria> criteriaList = new ArrayList<>();

        if (serviceName != null && !serviceName.isBlank()) {
            criteriaList.add(Criteria.where("serviceName").is(serviceName));
        }

        if (action != null && !action.isBlank()) {
            criteriaList.add(Criteria.where("action").is(action));
        }

        if (userId != null) {
            criteriaList.add(Criteria.where("userIdentifier").is(userId));
        }

        if (startDate != null) {
            criteriaList.add(Criteria.where("timestamp").gte(startDate));
        }

        if (endDate != null) {
            criteriaList.add(Criteria.where("timestamp").lte(endDate));
        }

        if (!criteriaList.isEmpty()) {
            criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria)
                .with(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "timestamp")));

        List<AuditLog> logs = mongoTemplate.find(query, AuditLog.class);
        
        return PageableExecutionUtils.getPage(
            logs, 
            PageRequest.of(page, pageSize),
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), AuditLog.class)
        );
    }

    public AuditLog getAuditLog(String id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Audit log not found: " + id));
    }

    public Page<AuditLog> getAuditsByUser(UUID userId, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        Query query = new Query(Criteria.where("userIdentifier").is(userId))
                .with(pageRequest);

        List<AuditLog> logs = mongoTemplate.find(query, AuditLog.class);
        
        return PageableExecutionUtils.getPage(
            logs, 
            pageRequest,
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), AuditLog.class)
        );
    }

    public Page<AuditLog> getAuditsByService(String serviceName, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        Query query = new Query(Criteria.where("serviceName").is(serviceName))
                .with(pageRequest);

        List<AuditLog> logs = mongoTemplate.find(query, AuditLog.class);
        
        return PageableExecutionUtils.getPage(
            logs, 
            pageRequest,
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), AuditLog.class)
        );
    }

    public void logAudit(AuditLog auditLog) {
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
        log.debug("Audit log created: {}", auditLog.getId());
    }
}

