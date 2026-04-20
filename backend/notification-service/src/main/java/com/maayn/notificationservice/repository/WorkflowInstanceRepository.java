package com.maayn.notificationservice.repository;

import com.maayn.notificationservice.entity.WorkflowInstance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowInstanceRepository extends MongoRepository<WorkflowInstance, UUID> {
    
    Optional<WorkflowInstance> findByEntityTypeAndEntityId(String entityType, UUID entityId);
    
    List<WorkflowInstance> findByStatus(String status);
    
    List<WorkflowInstance> findByEntityType(String entityType);
}

