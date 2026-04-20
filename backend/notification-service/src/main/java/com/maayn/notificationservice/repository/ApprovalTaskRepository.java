package com.maayn.notificationservice.repository;

import com.maayn.notificationservice.entity.ApprovalTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalTaskRepository extends MongoRepository<ApprovalTask, UUID> {
    
    List<ApprovalTask> findByWorkflowId(UUID workflowId);
    
    List<ApprovalTask> findByAssignedToAndTaskStatus(UUID assignedTo, String taskStatus);
    
    List<ApprovalTask> findByRoleAndTaskStatus(String role, String taskStatus);
    
    Optional<ApprovalTask> findByWorkflowIdAndStep(UUID workflowId, int step);
}

