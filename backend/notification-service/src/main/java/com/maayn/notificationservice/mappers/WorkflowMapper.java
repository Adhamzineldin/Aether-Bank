package com.maayn.notificationservice.mappers;

import com.maayn.notificationservice.documents.workflow.ApprovalTaskDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowInstanceDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowStepDocument;
import maayn.veld.generated.models.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class WorkflowMapper {

    public WorkflowInstance toModel(WorkflowInstanceDocument doc) {
        if (doc == null) return null;
        return new WorkflowInstance(
                doc.getId() != null ? UUID.fromString(doc.getId()) : null,
                doc.getTemplateId(),
                doc.getEntityType(),
                doc.getEntityId(),
                doc.getStatus(),
                doc.getCurrentStep(),
                doc.getSteps() != null
                        ? doc.getSteps().stream().map(this::stepToModel).collect(Collectors.toList())
                        : List.of(),
                doc.getVersion(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    private WorkflowStep stepToModel(WorkflowStepDocument doc) {
        if (doc == null) return null;

        // If WorkflowStep also has a protected constructor, use the All-Args constructor here too
        return new WorkflowStep(
                doc.getId() != null ? UUID.fromString(doc.getId()) : null,
                doc.getStep(),
                doc.getRole(),
                doc.getAction()
        );
    }

    public List<WorkflowStepDocument> cloneStepsFromTemplate(List<WorkflowStepDocument> templateSteps) {
        if (templateSteps == null || templateSteps.isEmpty()) {
            return List.of();
        }
        return templateSteps.stream().map(this::cloneStep).collect(Collectors.toList());
    }

    private WorkflowStepDocument cloneStep(WorkflowStepDocument source) {
        return WorkflowStepDocument.builder()
                .id(UUID.randomUUID().toString())
                .step(source.getStep())
                .role(source.getRole())
                .action(source.getAction())
                .build();
    }

    public WorkflowInstanceDocument toDocument(CreateWorkflowInput input, List<WorkflowStepDocument> steps) {
        boolean hasSteps = steps != null && !steps.isEmpty();
        WorkflowStatus status = hasSteps ? WorkflowStatus.IN_PROGRESS : WorkflowStatus.APPROVED;
        long currentStep = hasSteps ? 1L : 0L;
        return WorkflowInstanceDocument.builder()
                .id(UUID.randomUUID().toString())
                .templateId(input.getTemplateId())
                .entityType(input.getEntityType())
                .entityId(input.getEntityId())
                .status(status)
                .currentStep(currentStep)
                .steps(steps != null ? steps : List.of())
                .version(1L)
                .build();
    }

    public ApprovalTask toApprovalModel(ApprovalTaskDocument doc) {
        if (doc == null) {
            return null;
        }
        return new ApprovalTask(
                doc.getId() != null ? UUID.fromString(doc.getId()) : null,
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
                doc.getCompletedAt()
        );
    }

    public ApprovalTaskDocument toTaskDocument(WorkflowInstanceDocument workflow,
                                               WorkflowStepDocument step) {
        return ApprovalTaskDocument.builder()
                .id(UUID.randomUUID().toString())
                .workflowId(workflow.getId() != null ? UUID.fromString(workflow.getId()) : null)
                .step(step.getStep())
                .role(step.getRole())
                .assignedTo(null)
                .taskStatus(TaskStatus.PENDING)
                .decisionStatus(DecisionStatus.PENDING)
                .build();
    }
}