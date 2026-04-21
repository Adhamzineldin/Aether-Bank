package com.maayn.notificationservice.mappers;

import com.maayn.notificationservice.documents.workflow.ApprovalTaskDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowInstanceDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowStepDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowTemplateDocument;
import maayn.veld.generated.models.workflow.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class WorkflowMapper {

    public WorkflowInstance toModel(WorkflowInstanceDocument doc) {
        if (doc == null)
            return null;
        return new WorkflowInstance(
                doc.getId() != null ? doc.getId() : null,
                doc.getTemplateId(),
                doc.getTemplateVersion(),
                doc.getEntityType(),
                doc.getEntityId(),
                doc.getStatus(),
                doc.getStatusReason(),
                doc.getCurrentStep(),
                doc.getVersion(),
                doc.getCompletedAt(),
                doc.getCompletedBy(),
                doc.getCreatedAt(),
                doc.getUpdatedAt());
    }

    public List<WorkflowStepDocument> cloneStepsFromTemplate(List<WorkflowStepDocument> templateSteps) {
        if (templateSteps == null || templateSteps.isEmpty()) {
            return List.of();
        }
        List<WorkflowStepDocument> clonedSteps = new ArrayList<>(templateSteps.size());
        for (WorkflowStepDocument source : templateSteps) {
            clonedSteps.add(WorkflowStepDocument.builder()
                    .id(UUID.randomUUID())
                    .step(source.getStep())
                    .role(source.getRole())
                    .action(source.getAction())
                    .build());
        }
        return clonedSteps;
    }

    public WorkflowInstanceDocument toDocument(CreateWorkflowInput input, List<WorkflowStepDocument> steps) {
        boolean hasSteps = steps != null && !steps.isEmpty();
        WorkflowStatus status = hasSteps ? WorkflowStatus.IN_PROGRESS : WorkflowStatus.APPROVED;
        int currentStep = hasSteps ? 1 : 0;
        return WorkflowInstanceDocument.builder()
                .id(UUID.randomUUID())
                .templateId(input.getTemplateId())
                .templateVersion(input.getTemplateVersion())
                .entityType(input.getEntityType())
                .entityId(input.getEntityId())
                .status(status)
                .statusReason(hasSteps ? "Workflow started" : "Auto-approved: no steps")
                .completedBy(null)
                .currentStep(currentStep)
                .steps(steps)
                .version(1)
                .build();
    }

    public WorkflowInstanceDocument toDocument(CreateWorkflowInput input, WorkflowTemplateDocument template) {
        List<WorkflowStepDocument> steps = template != null ? cloneStepsFromTemplate(template.getSteps()) : List.of();
        return toDocument(input, steps);
    }

    public ApprovalTask toApprovalModel(ApprovalTaskDocument doc) {
        if (doc == null) {
            return null;
        }
        return new ApprovalTask(
                doc.getId() != null ? doc.getId() : null,
                doc.getWorkflowId(),
                doc.getStep(),
                doc.getRole(),
                doc.getAssignedTo(),
                doc.getTaskStatus(),
                doc.getDecisionStatus(),
                doc.getDecidedBy(),
                doc.getDecisionAt(),
                doc.getDecisionComment(),
                doc.getCreatedAt(),
                doc.getCompletedAt());
    }

    public ApprovalTaskDocument toTaskDocument(WorkflowInstanceDocument workflow, WorkflowStepDocument step) {
        return toTaskDocument(workflow, step, null);
    }

    public ApprovalTaskDocument toTaskDocument(WorkflowInstanceDocument workflow,
            WorkflowStepDocument step,
            UUID assignedUser) {

        if (workflow == null || step == null) {
            throw new IllegalArgumentException("Workflow and step must not be null");
        }
        return ApprovalTaskDocument.builder()
                .id(UUID.randomUUID())
                .workflowId(workflow.getId())
                .step(step.getStep())
                .role(step.getRole())
                .assignedTo(assignedUser)
                .taskStatus(TaskStatus.PENDING)
                .decisionStatus(DecisionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}