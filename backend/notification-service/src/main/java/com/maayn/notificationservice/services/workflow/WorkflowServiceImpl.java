package com.maayn.notificationservice.services.workflow;

import com.maayn.notificationservice.entity.ApprovalTask;
import com.maayn.notificationservice.entity.WorkflowInstance;
import com.maayn.notificationservice.entity.WorkflowStep;
import com.maayn.notificationservice.entity.WorkflowTemplate;
import com.maayn.notificationservice.repository.ApprovalTaskRepository;
import com.maayn.notificationservice.repository.WorkflowInstanceRepository;
import com.maayn.notificationservice.repository.WorkflowTemplateRepository;
import com.maayn.notificationservice.support.WorkflowCallerRoles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.ApiException;
import maayn.veld.generated.models.workflow.CreateWorkflowInput;
import maayn.veld.generated.models.workflow.DecisionStatus;
import maayn.veld.generated.models.shared.StepAction;
import maayn.veld.generated.models.workflow.StepRole;
import maayn.veld.generated.models.workflow.TaskActionInput;
import maayn.veld.generated.models.workflow.TaskDecision;
import maayn.veld.generated.models.workflow.TaskStatus;
import maayn.veld.generated.models.workflow.UpdateWorkflowInput;
import maayn.veld.generated.models.workflow.WorkflowStatus;
import maayn.veld.generated.services.IWorkflowService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Persistent workflow / approval-task implementation backing the
 * Veld-generated {@code WorkflowController}. Replaces the earlier in-memory
 * stub so approval inboxes show real tasks and decisions actually advance the
 * workflow.
 *
 * <p>Each task is stamped with a step role ({@code RISK}/{@code MANAGER}/{@code DIRECTOR}).
 * The API gateway forwards {@code X-User-Roles} from the JWT; only callers whose
 * roles include that step role (or {@code ADMIN}/{@code SUPERADMIN}) see tasks in
 * {@link #getTasks} and may call {@link #decideTask}.</p>
 *
 * <p>When a workflow completes we publish {@code loan.approved},
 * {@code mortgage.approved}, or {@code certificate.approved} on the legacy
 * {@code bank.events} exchange so financial-service can
 * disburse.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowServiceImpl implements IWorkflowService {

    private static final String LEGACY_BANK_EVENTS_EXCHANGE = "bank.events";
    private static final String LOAN_APPROVED_ROUTING_KEY = "loan.approved";
    private static final String MORTGAGE_APPROVED_ROUTING_KEY = "mortgage.approved";
    private static final String CERTIFICATE_APPROVED_ROUTING_KEY = "certificate.approved";

    private final WorkflowInstanceRepository instanceRepository;
    private final ApprovalTaskRepository taskRepository;
    private final WorkflowTemplateRepository templateRepository;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Override
    public maayn.veld.generated.models.workflow.WorkflowInstance createWorkflow(CreateWorkflowInput input) {
        WorkflowTemplate template = templateRepository.findByEntityType(input.getEntityType())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No workflow template for entity type " + input.getEntityType()));

        WorkflowInstance instance = WorkflowInstance.builder()
                .id(UUID.randomUUID())
                .templateId(template.getId())
                .entityType(input.getEntityType())
                .entityId(input.getEntityId())
                .status("PENDING")
                .currentStep(1)
                .steps(template.getSteps())
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        instanceRepository.save(instance);

        createTaskForCurrentStep(instance);

        return toSdk(instance);
    }

    @Override
    public maayn.veld.generated.models.workflow.WorkflowInstance updateWorkflow(String id, UpdateWorkflowInput input) {
        WorkflowInstance instance = instanceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + id));
        if (input.getStatus() != null) instance.setStatus(input.getStatus().getValue());
        if (input.getCurrentStep() != null) instance.setCurrentStep(input.getCurrentStep());
        instance.setUpdatedAt(LocalDateTime.now());
        instance.setVersion(instance.getVersion() + 1);
        instanceRepository.save(instance);
        return toSdk(instance);
    }

    @Override
    public maayn.veld.generated.models.workflow.WorkflowInstance getWorkflow(String id) {
        return instanceRepository.findById(UUID.fromString(id))
                .map(this::toSdk)
                .orElse(null);
    }

    @Override
    public List<maayn.veld.generated.models.workflow.WorkflowInstance> getWorkflows() {
        return instanceRepository.findAll().stream().map(this::toSdk).toList();
    }

    /**
     * Open tasks visible to this caller based on {@code X-User-Roles} (set by the gateway).
     */
    @Override
    public List<maayn.veld.generated.models.workflow.ApprovalTask> getTasks(String userId) {
        Set<String> roles = WorkflowCallerRoles.fromCurrentRequest();
        return taskRepository.findAll().stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getTaskStatus())
                        || "IN_PROGRESS".equalsIgnoreCase(t.getTaskStatus()))
                .filter(t -> WorkflowCallerRoles.mayActOnStep(t.getRole(), roles))
                .map(this::toSdk)
                .toList();
    }

    @Override
    public maayn.veld.generated.models.workflow.ApprovalTask decideTask(String taskId, TaskActionInput input) {
        UUID uuid = UUID.fromString(taskId);
        ApprovalTask task = taskRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        Set<String> roles = WorkflowCallerRoles.fromCurrentRequest();
        if (!WorkflowCallerRoles.mayActOnStep(task.getRole(), roles)) {
            throw new ApiException(
                    "WORKFLOW_FORBIDDEN",
                    403,
                    "This approval step requires role " + task.getRole()
                            + ". Your session does not include that role.");
        }

        if ("COMPLETED".equalsIgnoreCase(task.getTaskStatus())) {
            log.warn("Task {} already completed; returning existing record", taskId);
            return toSdk(task);
        }

        LocalDateTime now = LocalDateTime.now();
        TaskDecision decision = input.getDecision();

        task.setTaskStatus("COMPLETED");
        task.setDecidedBy(input.getEmployeeId());
        task.setDecisionAt(now);
        task.setDecisionComment(input.getComment());
        task.setCompletedAt(now);
        task.setDecisionStatus(decision == null ? "PENDING" : decision.getValue());
        taskRepository.save(task);

        advanceWorkflow(task.getWorkflowId(), decision);

        return toSdk(task);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private void advanceWorkflow(UUID workflowId, TaskDecision decision) {
        Optional<WorkflowInstance> maybe = instanceRepository.findById(workflowId);
        if (maybe.isEmpty()) {
            log.warn("decideTask: workflow {} vanished", workflowId);
            return;
        }
        WorkflowInstance instance = maybe.get();
        LocalDateTime now = LocalDateTime.now();

        if (decision == TaskDecision.REJECTED) {
            instance.setStatus("REJECTED");
            instance.setUpdatedAt(now);
            instance.setVersion(instance.getVersion() + 1);
            instanceRepository.save(instance);
            log.info("Workflow {} REJECTED at step {}", instance.getId(), instance.getCurrentStep());
            return;
        }

        int totalSteps = instance.getSteps() == null ? 0 : instance.getSteps().size();
        int nextStep = instance.getCurrentStep() + 1;

        if (nextStep > totalSteps) {
            instance.setStatus("APPROVED");
            instance.setUpdatedAt(now);
            instance.setVersion(instance.getVersion() + 1);
            instanceRepository.save(instance);
            log.info("Workflow {} fully APPROVED", instance.getId());
            publishEntityApproved(instance);
            return;
        }

        instance.setCurrentStep(nextStep);
        instance.setStatus("IN_PROGRESS");
        instance.setUpdatedAt(now);
        instance.setVersion(instance.getVersion() + 1);
        instanceRepository.save(instance);
        createTaskForCurrentStep(instance);
        log.info("Workflow {} advanced to step {}", instance.getId(), nextStep);
    }

    private void createTaskForCurrentStep(WorkflowInstance instance) {
        if (instance.getSteps() == null || instance.getSteps().isEmpty()) return;
        int current = instance.getCurrentStep();
        WorkflowStep step = instance.getSteps().stream()
                .filter(s -> s.getStep() == current)
                .findFirst()
                .orElse(null);
        if (step == null) {
            log.warn("Workflow {} has no step definition for currentStep {}", instance.getId(), current);
            return;
        }
        ApprovalTask task = ApprovalTask.builder()
                .id(UUID.randomUUID())
                .workflowId(instance.getId())
                .step(step.getStep())
                .role(step.getRole())
                .taskStatus("PENDING")
                .decisionStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        taskRepository.save(task);
    }

    private void publishEntityApproved(WorkflowInstance instance) {
        if (rabbitTemplate == null) {
            log.warn("RabbitTemplate not available — skipping bank.events publish for workflow {}", instance.getId());
            return;
        }
        String entityType = instance.getEntityType();
        if (entityType == null) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("workflowId", instance.getId().toString());
        String key;
        switch (entityType.toUpperCase(Locale.ROOT)) {
            case "LOAN" -> {
                payload.put("loanId", instance.getEntityId().toString());
                key = LOAN_APPROVED_ROUTING_KEY;
            }
            case "MORTGAGE" -> {
                payload.put("mortgageId", instance.getEntityId().toString());
                key = MORTGAGE_APPROVED_ROUTING_KEY;
            }
            case "CERTIFICATE" -> {
                payload.put("certificateId", instance.getEntityId().toString());
                key = CERTIFICATE_APPROVED_ROUTING_KEY;
            }
            default -> {
                log.warn("No bank.events routing for workflow entity type {}", entityType);
                return;
            }
        }
        try {
            rabbitTemplate.convertAndSend(LEGACY_BANK_EVENTS_EXCHANGE, key, payload);
            log.info("Published {} for {} {}", key, entityType, instance.getEntityId());
        } catch (Exception ex) {
            log.error("Failed to publish {} for {}", key, instance.getEntityId(), ex);
        }
    }

    // ------------------------------------------------------------------
    // entity → SDK mappers
    // ------------------------------------------------------------------

    private maayn.veld.generated.models.workflow.WorkflowInstance toSdk(WorkflowInstance e) {
        maayn.veld.generated.models.workflow.WorkflowInstance dto =
                new maayn.veld.generated.models.workflow.WorkflowInstance();
        dto.setId(e.getId());
        dto.setTemplateId(e.getTemplateId());
        dto.setEntityType(e.getEntityType());
        dto.setEntityId(e.getEntityId());
        dto.setStatus(parseWorkflowStatus(e.getStatus()));
        dto.setCurrentStep(e.getCurrentStep());
        dto.setVersion(e.getVersion());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        // Steps are no longer part of the WorkflowInstance contract; clients
        // should query ApprovalTask endpoints for per-step details.
        return dto;
    }

    private List<maayn.veld.generated.models.shared.WorkflowStep> toSdkSteps(List<WorkflowStep> steps) {
        if (steps == null) return List.of();
        return steps.stream()
                .map(s -> new maayn.veld.generated.models.shared.WorkflowStep(
                        s.getId(),
                        s.getStep(),
                        parseRole(s.getRole()),
                        parseAction(s.getAction())))
                .toList();
    }

    private maayn.veld.generated.models.workflow.ApprovalTask toSdk(ApprovalTask t) {
        return new maayn.veld.generated.models.workflow.ApprovalTask(
                t.getId(),
                t.getWorkflowId(),
                t.getStep(),
                parseRole(t.getRole()),
                t.getAssignedTo(),
                parseTaskStatus(t.getTaskStatus()),
                parseDecisionStatus(t.getDecisionStatus()),
                t.getDecidedBy(),
                t.getDecisionAt(),
                t.getDecisionComment(),
                t.getCreatedAt(),
                t.getCompletedAt()
        );
    }

    private static StepRole parseRole(String raw) {
        if (raw == null) return null;
        try { return StepRole.valueOf(raw); } catch (IllegalArgumentException ignored) { return null; }
    }

    private static StepAction parseAction(String raw) {
        if (raw == null) return null;
        try { return StepAction.valueOf(raw); } catch (IllegalArgumentException ignored) { return null; }
    }

    private static TaskStatus parseTaskStatus(String raw) {
        if (raw == null) return null;
        try { return TaskStatus.valueOf(raw); } catch (IllegalArgumentException ignored) { return null; }
    }

    private static DecisionStatus parseDecisionStatus(String raw) {
        if (raw == null) return null;
        try { return DecisionStatus.valueOf(raw); } catch (IllegalArgumentException ignored) { return null; }
    }

    private static WorkflowStatus parseWorkflowStatus(String raw) {
        if (raw == null) return null;
        try { return WorkflowStatus.valueOf(raw); } catch (IllegalArgumentException ignored) { return null; }
    }
}
