package com.maayn.iamservice.repository;

import com.maayn.iamservice.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    List<AuditLog> findByUserId(UUID userId);
    
    List<AuditLog> findByAction(String action);
    
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<AuditLog> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
