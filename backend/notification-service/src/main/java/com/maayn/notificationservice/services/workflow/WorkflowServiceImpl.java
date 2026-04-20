package com.maayn.notificationservice.services.workflow;

import com.maayn.notificationservice.entity.ApprovalTask;
import com.maayn.notificationservice.entity.WorkflowInstance;
import com.maayn.notificationservice.entity.WorkflowStep;
import com.maayn.notificationservice.entity.WorkflowTemplate;
import com.maayn.notificationservice.repository.ApprovalTaskRepository;
import com.maayn.notificationservice.repository.WorkflowInstanceRepository;
import com.maayn.notificationservice.repository.WorkflowTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.*;
import maayn.veld.generated.services.IWorkflowService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowServiceImpl implements IWorkflowService {

    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final ApprovalTaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public WorkflowInstance createWorkflow(CreateWorkflowInput input) throws Exception {
        log.info("Creating workflow for entity: {} with ID: {}", input.getEntityType(), input.getEntityId());

        // Get template
        WorkflowTemplate template = templateRepository.findById(input.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Workflow template not found: " + input.getTemplateId()));

        // Create workflow instance
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

        WorkflowInstance savedInstance = instanceRepository.save(instance);

        // Create first approval task
        if (!template.getSteps().isEmpty()) {
            WorkflowStep firstStep = template.getSteps().get(0);
            createApprovalTask(savedInstance, firstStep);
        }

        log.info("Workflow created: {}", savedInstance.getId());
        return savedInstance;
    }

    @Override
    public WorkflowInstance updateWorkflow(String id, UpdateWorkflowInput input) throws Exception {
        UUID workflowId = UUID.fromString(id);
        WorkflowInstance instance = instanceRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));

        if (input.getStatus() != null) {
            instance.setStatus(input.getStatus().name());
        }
        if (input.getCurrentStep() != null) {
            instance.setCurrentStep(input.getCurrentStep());
        }
        instance.setUpdatedAt(LocalDateTime.now());
        instance.setVersion(instance.getVersion() + 1);

        return instanceRepository.save(instance);
    }

    @Override
    public WorkflowInstance getWorkflow(String id) throws Exception {
        UUID workflowId = UUID.fromString(id);
        return instanceRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));
    }

    @Override
    public List<WorkflowInstance> getWorkflows() throws Exception {
        return instanceRepository.findAll();
    }

    @Override
    public List<ApprovalTask> getTasks(String id) throws Exception {
        UUID workflowId = UUID.fromString(id);
        return taskRepository.findByWorkflowId(workflowId);
    }

    @Override
    public ApprovalTask decideTask(String taskId, TaskActionInput input) throws Exception {
        UUID taskUUID = UUID.fromString(taskId);
        
        ApprovalTask task = taskRepository.findById(taskUUID)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (!"PENDING".equals(task.getDecisionStatus())) {
            throw new RuntimeException("Task already decided");
        }

        // Update task
        task.setDecisionStatus(input.getDecision().name());
        task.setDecidedBy(input.getEmployeeId());
        task.setDecisionAt(LocalDateTime.now());
        task.setDecisionComment(input.getComment());
        task.setTaskStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());

        ApprovalTask savedTask = taskRepository.save(task);

        // Update workflow
        WorkflowInstance workflow = instanceRepository.findById(task.getWorkflowId())
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        if ("APPROVED".equals(input.getDecision().name())) {
            processApproval(workflow, task);
        } else {
            processRejection(workflow, task);
        }

        return savedTask;
    }

    private void processApproval(WorkflowInstance workflow, ApprovalTask task) {
        log.info("Processing approval for workflow: {}, step: {}", workflow.getId(), task.getStep());

        // Check if this was the last step
        if (task.getStep() >= workflow.getSteps().size()) {
            // All steps approved - workflow complete
            workflow.setStatus("APPROVED");
            workflow.setUpdatedAt(LocalDateTime.now());
            instanceRepository.save(workflow);

            // Publish approval event
            publishApprovalEvent(workflow);
        } else {
            // Move to next step
            workflow.setCurrentStep(workflow.getCurrentStep() + 1);
            workflow.setStatus("IN_PROGRESS");
            workflow.setUpdatedAt(LocalDateTime.now());
            instanceRepository.save(workflow);

            // Create next task
            WorkflowStep nextStep = workflow.getSteps().get(workflow.getCurrentStep() - 1);
            createApprovalTask(workflow, nextStep);
        }
    }

    private void processRejection(WorkflowInstance workflow, ApprovalTask task) {
        log.info("Processing rejection for workflow: {}, step: {}", workflow.getId(), task.getStep());

        workflow.setStatus("REJECTED");
        workflow.setUpdatedAt(LocalDateTime.now());
        instanceRepository.save(workflow);

        // Publish rejection event
        publishRejectionEvent(workflow);
    }

    private void createApprovalTask(WorkflowInstance workflow, WorkflowStep step) {
        ApprovalTask task = ApprovalTask.builder()
                .id(UUID.randomUUID())
                .workflowId(workflow.getId())
                .step(step.getStep())
                .role(step.getRole())
                .assignedTo(null) // TODO: Assign to specific employee based on role
                .taskStatus("PENDING")
                .decisionStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        taskRepository.save(task);
        log.info("Created approval task: {} for workflow: {}", task.getId(), workflow.getId());
    }

    private void publishApprovalEvent(WorkflowInstance workflow) {
        try {
            String routingKey = workflow.getEntityType().toLowerCase() + ".approved";
            LoanApprovedEvent event = new LoanApprovedEvent();
            // TODO: Set event properties based on workflow
            
            rabbitTemplate.convertAndSend("bank.events", routingKey, event);
            log.info("Published approval event for {} ID: {}", workflow.getEntityType(), workflow.getEntityId());
        } catch (Exception e) {
            log.error("Failed to publish approval event", e);
        }
    }

    private void publishRejectionEvent(WorkflowInstance workflow) {
        try {
            String routingKey = workflow.getEntityType().toLowerCase() + ".rejected";
            LoanRejectedEvent event = new LoanRejectedEvent();
            // TODO: Set event properties
            
            rabbitTemplate.convertAndSend("bank.events", routingKey, event);
            log.info("Published rejection event for {} ID: {}", workflow.getEntityType(), workflow.getEntityId());
        } catch (Exception e) {
            log.error("Failed to publish rejection event", e);
        }
    }
}

// Placeholder event classes (should be moved to separate files)
class LoanApprovedEvent {}
class LoanRejectedEvent {}
