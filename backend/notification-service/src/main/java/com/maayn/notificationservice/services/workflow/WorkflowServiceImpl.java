package com.maayn.notificationservice.services.workflow;

import maayn.veld.generated.models.workflow.*;
import maayn.veld.generated.services.IWorkflowService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Minimal in-memory implementation of {@link IWorkflowService} so the
 * auto-generated {@code WorkflowController} can be wired by Spring.
 *
 * <p>This is a stub to unblock notification-service startup; replace with a
 * real MongoDB-backed implementation using the existing
 * {@code WorkflowInstanceRepository} / {@code ApprovalTaskRepository}
 * entities when business logic is finalised.</p>
 */
@Service
public class WorkflowServiceImpl implements IWorkflowService {

    private final Map<UUID, WorkflowInstance> workflows = new ConcurrentHashMap<>();
    private final Map<UUID, ApprovalTask> tasks = new ConcurrentHashMap<>();

    @Override
    public WorkflowInstance createWorkflow(CreateWorkflowInput input) {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        WorkflowInstance wf = new WorkflowInstance(
                id,
                input.getTemplateId(),
                input.getEntityType(),
                input.getEntityId(),
                WorkflowStatus.PENDING,
                0,
                new ArrayList<>(),
                1,
                now,
                now
        );
        workflows.put(id, wf);
        return wf;
    }

    @Override
    public WorkflowInstance updateWorkflow(String id, UpdateWorkflowInput input) {
        UUID uuid = UUID.fromString(id);
        WorkflowInstance wf = workflows.get(uuid);
        if (wf == null) return null;
        if (input.getStatus() != null) wf.setStatus(input.getStatus());
        if (input.getCurrentStep() != null) wf.setCurrentStep(input.getCurrentStep());
        wf.setUpdatedAt(LocalDateTime.now());
        wf.setVersion((wf.getVersion() == null ? 0 : wf.getVersion()) + 1);
        return wf;
    }

    @Override
    public WorkflowInstance getWorkflow(String id) {
        return workflows.get(UUID.fromString(id));
    }

    @Override
    public List<WorkflowInstance> getWorkflows() {
        return new ArrayList<>(workflows.values());
    }

    @Override
    public List<ApprovalTask> getTasks(String userId) {
        UUID uuid;
        try { uuid = UUID.fromString(userId); } catch (IllegalArgumentException e) { return List.of(); }
        return tasks.values().stream()
                .filter(t -> uuid.equals(t.getAssignedTo()))
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalTask decideTask(String taskId, TaskActionInput input) {
        UUID uuid = UUID.fromString(taskId);
        ApprovalTask task = tasks.get(uuid);
        if (task == null) {
            // Create a lightweight task record so callers get a deterministic response
            task = new ApprovalTask(
                    uuid, UUID.randomUUID(), 0, StepRole.MANAGER, input.getEmployeeId(),
                    TaskStatus.COMPLETED, null, input.getEmployeeId(),
                    LocalDateTime.now(), input.getComment(),
                    LocalDateTime.now(), LocalDateTime.now());
        } else {
            task.setTaskStatus(TaskStatus.COMPLETED);
            task.setDecidedBy(input.getEmployeeId());
            task.setDecisionAt(LocalDateTime.now());
            task.setDecisionComment(input.getComment());
            task.setCompletedAt(LocalDateTime.now());
        }
        task.setDecisionStatus(mapDecision(input.getDecision()));
        tasks.put(uuid, task);
        return task;
    }

    private DecisionStatus mapDecision(TaskDecision d) {
        if (d == null) return null;
        try {
            return DecisionStatus.valueOf(d.name());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}


