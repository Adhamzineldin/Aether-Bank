package com.maayn.notificationservice.repositories;

import com.maayn.notificationservice.documents.workflow.ApprovalTaskDocument;
import maayn.veld.generated.models.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalTaskRepository extends MongoRepository<ApprovalTaskDocument, String> {
        List<ApprovalTaskDocument> findByAssignedTo(UUID assignedTo);
        List<ApprovalTaskDocument> findByWorkflowId(UUID workflowId);
        List<ApprovalTaskDocument> findByAssignedToAndTaskStatus(UUID assignedTo, TaskStatus taskStatus);
}