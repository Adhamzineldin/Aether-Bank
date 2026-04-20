package com.maayn.notificationservice.repository;

import com.maayn.notificationservice.entity.WorkflowTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTemplateRepository extends MongoRepository<WorkflowTemplate, UUID> {
    
    Optional<WorkflowTemplate> findByEntityType(String entityType);
}

