package com.maayn.notificationservice.mappers;

import com.maayn.notificationservice.documents.workflow.ApprovalTaskDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowInstanceDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowStepDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowTemplateDocument;
import maayn.veld.generated.models.workflow.*;
import maayn.veld.generated.models.shared.StepAction;
import maayn.veld.generated.models.shared.WorkflowStep;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bridges Mongo document models (strings) with the generated SDK models
 * (enums + trimmed field set). The document side keeps extra persistence-only
 * fields like {@code statusReason}, {@code completedAt}, {@code completedBy}
 * and {@code templateVersion}; those are intentionally dropped when exposing
 * a workflow through the SDK contract.
 */
@Component
public class WorkflowMapper {

    public WorkflowInstance toModel(WorkflowInstanceDocument doc) {
        if (doc == null)
            return null;
        WorkflowInstance instance = new WorkflowInstance();
        instance.setId(doc.getId());
        instance.setTemplateId(doc.getTemplateId());
        instance.setTemplateVersion(doc.getTemplateVersion());
        instance.setEntityType(doc.getEntityType());
        instance.setEntityId(doc.getEntityId());
        instance.setStatus(doc.getStatus());
        instance.setCurrentStep(doc.getCurrentStep());
        instance.setVersion(doc.getVersion());
        instance.setCompletedBy(doc.getCompletedBy());
        instance.setCreatedAt(doc.getCreatedAt());
        instance.setUpdatedAt(doc.getUpdatedAt());
        // Note: workflow steps are no longer part of the WorkflowInstance contract
        // (commented out in the Veld model). Step details are exposed via the
        // ApprovalTask endpoints instead.
        return instance;
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
                // The SDK input does not carry a template version yet; pin to 1
                // until the contract is extended.
                .templateVersion(1)
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
                .role(parseRole(step.getRole()))
                .assignedTo(assignedUser)
                .taskStatus(TaskStatus.PENDING)
                .decisionStatus(DecisionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<WorkflowStep> toModelSteps(List<WorkflowStepDocument> docs) {
        if (docs == null || docs.isEmpty()) {
            return List.of();
        }
        List<WorkflowStep> out = new ArrayList<>(docs.size());
        for (WorkflowStepDocument d : docs) {
            out.add(new WorkflowStep(d.getId(), d.getStep(), parseRole(d.getRole()), parseAction(d.getAction())));
        }
        return out;
    }

    private static StepRole parseRole(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return StepRole.fromValue(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static StepAction parseAction(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return StepAction.fromValue(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
