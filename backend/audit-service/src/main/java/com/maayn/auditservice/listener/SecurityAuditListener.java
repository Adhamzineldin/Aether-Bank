package com.maayn.auditservice.listener;

import com.maayn.auditservice.entity.AuditLog;
import com.maayn.auditservice.repository.AuditLogRepository;
import com.maayn.auditservice.config.RabbitMQConfig;
import com.maayn.auditservice.mapper.AuditMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.AuditEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditListener {

    private final AuditLogRepository repository;

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void receiveAuditLog(AuditEvent event) {
        log.info("SECURITY AUDIT CAUGHT: [{}] action '{}' from {}",
                event.getStatus(), event.getAction(), event.getServiceName());

    
        AuditLog logEntry = AuditMapper.toEntity(event);
        
        repository.save(logEntry);

        log.debug("Log permanently saved to MongoDB");
    }
}