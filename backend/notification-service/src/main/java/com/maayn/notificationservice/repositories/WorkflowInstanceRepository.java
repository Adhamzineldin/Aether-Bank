package com.maayn.notificationservice.repositories;

import com.maayn.notificationservice.documents.workflow.WorkflowInstanceDocument;
import maayn.veld.generated.models.WorkflowStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowInstanceRepository extends MongoRepository<WorkflowInstanceDocument, String> {
    List<WorkflowInstanceDocument> findByEntityId(UUID entityId);
    List<WorkflowInstanceDocument> findByStatus(WorkflowStatus status);
}
