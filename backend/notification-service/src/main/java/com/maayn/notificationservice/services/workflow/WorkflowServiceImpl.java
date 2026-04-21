package com.maayn.notificationservice.services.workflow;

import com.maayn.notificationservice.documents.workflow.ApprovalTaskDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowInstanceDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowStepDocument;
import com.maayn.notificationservice.documents.workflow.WorkflowTemplateDocument;
import com.maayn.notificationservice.mappers.WorkflowMapper;
import com.maayn.notificationservice.repositories.ApprovalTaskRepository;
import com.maayn.notificationservice.repositories.WorkflowInstanceRepository;
import com.maayn.notificationservice.repositories.WorkflowTemplateRepository;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.BadRequestException;
import maayn.veld.generated.errors.ForbiddenException;
import maayn.veld.generated.errors.NotFoundException;
import maayn.veld.generated.models.workflow.*;
import maayn.veld.generated.services.IWorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements IWorkflowService {

    private final WorkflowInstanceRepository workflowRepo;
    private final WorkflowTemplateRepository templateRepo;
    private final ApprovalTaskRepository taskRepo;
    private final WorkflowMapper workflowMapper;

    @Override
    public WorkflowInstance createWorkflow(CreateWorkflowInput input) throws Exception {
        if (input == null || input.getTemplateId() == null) {
            throw new BadRequestException("templateId is required");
        }
        if (!StringUtils.hasText(input.getEntityType()) || input.getEntityId() == null) {
            throw new BadRequestException("entityType and entityId are required");
        }

        WorkflowTemplateDocument template = templateRepo.findById(input.getTemplateId().toString())
                .orElseThrow(() -> new NotFoundException("Workflow template not found"));

        if (StringUtils.hasText(template.getEntityType())
                && !template.getEntityType().equalsIgnoreCase(input.getEntityType())) {
            throw new BadRequestException("entityType does not match template");
        }

        List<WorkflowStepDocument> steps = workflowMapper.cloneStepsFromTemplate(template.getSteps());
        steps.sort(Comparator.comparing(WorkflowStepDocument::getStep));

        WorkflowInstanceDocument doc = workflowMapper.toDocument(input, steps);
        WorkflowInstanceDocument saved = workflowRepo.save(doc);

        if (!steps.isEmpty()) {
            taskRepo.save(workflowMapper.toTaskDocument(saved, steps.get(0)));
        }

        return workflowMapper.toModel(saved);
    }

    @Override
    public WorkflowInstance getWorkflow(String id) throws Exception {
        return workflowRepo.findById(requireUuidString(id))
                .map(workflowMapper::toModel)
                .orElseThrow(() -> new NotFoundException("Workflow not found"));
    }

    @Override
    public List<WorkflowInstance> getWorkflows() throws Exception {
        return workflowRepo.findAll().stream()
                .map(workflowMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public WorkflowInstance updateWorkflow(String id, UpdateWorkflowInput input) throws Exception {
        WorkflowInstanceDocument doc = workflowRepo.findById(requireUuidString(id))
                .orElseThrow(() -> new NotFoundException("Workflow not found"));
        if (input.getStatus() != null) {
            doc.setStatus(input.getStatus());
        }
        if (input.getCurrentStep() != null) {
            doc.setCurrentStep(input.getCurrentStep());
        }
        return workflowMapper.toModel(workflowRepo.save(doc));
    }

    @Override
    public List<ApprovalTask> getTasks(String id) throws Exception {
        String workflowKey = requireUuidString(id);
        if (workflowRepo.findById(workflowKey).isEmpty()) {
            throw new NotFoundException("Workflow not found");
        }
        UUID wfUuid = UUID.fromString(workflowKey);
        return taskRepo.findByWorkflowId(wfUuid).stream()
            .sorted(Comparator.comparing(ApprovalTaskDocument::getStep, Comparator.nullsLast(Integer::compareTo)))
                .map(workflowMapper::toApprovalModel)
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalTask decideTask(String taskId, TaskActionInput input) throws Exception {
        if (input == null || input.getDecision() == null || input.getEmployeeId() == null) {
            throw new BadRequestException("employeeId and decision are required");
        }

        ApprovalTaskDocument task = taskRepo.findById(requireUuidString(taskId))
                .orElseThrow(() -> new NotFoundException("Approval task not found"));

        if (task.getDecisionStatus() != DecisionStatus.PENDING) {
            throw new BadRequestException("Task is already decided");
        }

        if (task.getAssignedTo() != null && !task.getAssignedTo().equals(input.getEmployeeId())) {
            throw new ForbiddenException("Task is assigned to another user");
        }

        LocalDateTime now = LocalDateTime.now();
        task.setTaskStatus(TaskStatus.COMPLETED);
        task.setDecisionStatus(input.getDecision() == TaskDecision.APPROVED
                ? DecisionStatus.APPROVED
                : DecisionStatus.REJECTED);
        task.setDecidedBy(input.getEmployeeId());
        task.setDecisionAt(now);
        task.setDecisionComment(input.getComment());
        task.setCompletedAt(now);
        taskRepo.save(task);

        WorkflowInstanceDocument wf = workflowRepo.findById(task.getWorkflowId().toString())
                .orElseThrow(() -> new NotFoundException("Workflow not found"));

        if (task.getDecisionStatus() == DecisionStatus.REJECTED) {
            wf.setStatus(WorkflowStatus.REJECTED);
            workflowRepo.save(wf);
            return workflowMapper.toApprovalModel(task);
        }

        if (task.getStep() == null) {
            throw new BadRequestException("Task step is not set");
        }

        List<WorkflowStepDocument> stepList = wf.getSteps() != null ? wf.getSteps() : List.of();
        int maxStep = stepList.stream()
            .mapToInt(WorkflowStepDocument::getStep)
                .max()
            .orElse(0);

        if (task.getStep() >= maxStep) {
            wf.setStatus(WorkflowStatus.APPROVED);
            workflowRepo.save(wf);
            return workflowMapper.toApprovalModel(task);
        }

        int nextStepNumber = task.getStep() + 1;
        wf.setCurrentStep(nextStepNumber);
        wf.setStatus(WorkflowStatus.IN_PROGRESS);
        workflowRepo.save(wf);

        WorkflowStepDocument nextStep = stepByNumber(wf, nextStepNumber)
                .orElseThrow(() -> new BadRequestException("Next workflow step not defined"));
        taskRepo.save(workflowMapper.toTaskDocument(wf, nextStep));

        return workflowMapper.toApprovalModel(task);
    }

    private static Optional<WorkflowStepDocument> stepByNumber(WorkflowInstanceDocument wf, int stepNumber) {
        if (wf.getSteps() == null) {
            return Optional.empty();
        }
        return wf.getSteps().stream()
                .filter(s -> s.getStep() != null && Objects.equals(s.getStep(), stepNumber))
                .findFirst();
    }

    private static String requireUuidString(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BadRequestException("Invalid id");
        }
        try {
            UUID.fromString(raw.trim());
            return raw.trim();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid id format");
        }
    }
}
