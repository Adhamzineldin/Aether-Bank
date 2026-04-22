package com.maayn.notificationservice.init;

import com.maayn.notificationservice.entity.ApprovalTask;
import com.maayn.notificationservice.entity.WorkflowInstance;
import com.maayn.notificationservice.entity.WorkflowStep;
import com.maayn.notificationservice.repository.ApprovalTaskRepository;
import com.maayn.notificationservice.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * One-shot reconciliation: for any {@link WorkflowInstance} left in PENDING or
 * IN_PROGRESS before the listener learned to create tasks (see
 * {@code SubmittedApplicationWorkflowListener}), synthesize an {@link ApprovalTask} for its
 * current step so approvers can actually act on it.
 *
 * <p>Idempotent: does nothing when the expected task already exists.</p>
 */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class OrphanWorkflowTaskBackfill implements CommandLineRunner {

    private final WorkflowInstanceRepository instanceRepository;
    private final ApprovalTaskRepository taskRepository;

    @Override
    public void run(String... args) {
        int created = 0;
        for (WorkflowInstance instance : instanceRepository.findAll()) {
            String status = instance.getStatus();
            if (!"PENDING".equalsIgnoreCase(status) && !"IN_PROGRESS".equalsIgnoreCase(status)) continue;

            int step = instance.getCurrentStep();
            Optional<ApprovalTask> existing = taskRepository.findByWorkflowIdAndStep(instance.getId(), step);
            if (existing.isPresent()) continue;

            if (instance.getSteps() == null) continue;
            WorkflowStep def = instance.getSteps().stream()
                    .filter(s -> s.getStep() == step)
                    .findFirst()
                    .orElse(null);
            if (def == null) continue;

            ApprovalTask task = ApprovalTask.builder()
                    .id(UUID.randomUUID())
                    .workflowId(instance.getId())
                    .step(def.getStep())
                    .role(def.getRole())
                    .taskStatus("PENDING")
                    .decisionStatus("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();
            taskRepository.save(task);
            created++;
            log.info("Backfilled approval task for workflow {} step {} role {}",
                    instance.getId(), def.getStep(), def.getRole());
        }
        if (created > 0) log.info("OrphanWorkflowTaskBackfill: created {} missing tasks", created);
    }
}
