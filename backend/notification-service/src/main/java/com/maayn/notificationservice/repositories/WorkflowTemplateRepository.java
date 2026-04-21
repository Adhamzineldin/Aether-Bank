package com.maayn.notificationservice.repositories;

import com.maayn.notificationservice.documents.workflow.WorkflowTemplateDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkflowTemplateRepository extends MongoRepository<WorkflowTemplateDocument, String> {

    List<WorkflowTemplateDocument> findByEntityType(String entityType);
}
