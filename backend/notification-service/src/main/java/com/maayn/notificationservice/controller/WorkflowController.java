package com.maayn.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.workflow.ApprovalTask;
import maayn.veld.generated.models.workflow.CreateWorkflowInput;
import maayn.veld.generated.models.workflow.TaskActionInput;
import maayn.veld.generated.models.workflow.UpdateWorkflowInput;
import maayn.veld.generated.models.workflow.WorkflowInstance;
import maayn.veld.generated.services.IWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Hand-written REST façade for the Veld-defined {@link IWorkflowService}.
 * The Veld generator does not emit a workflow controller, so this class
 * exposes the routes the frontend needs to consume ({@code /api/workflow}).
 */
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final IWorkflowService workflowService;

    @GetMapping
    public ResponseEntity<List<WorkflowInstance>> listWorkflows() throws Exception {
        return ResponseEntity.ok(workflowService.getWorkflows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowInstance> getWorkflow(@PathVariable String id) throws Exception {
        WorkflowInstance wf = workflowService.getWorkflow(id);
        return wf == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(wf);
    }

    @PostMapping
    public ResponseEntity<WorkflowInstance> createWorkflow(@RequestBody CreateWorkflowInput input) throws Exception {
        return ResponseEntity.ok(workflowService.createWorkflow(input));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkflowInstance> updateWorkflow(
            @PathVariable String id,
            @RequestBody UpdateWorkflowInput input
    ) throws Exception {
        WorkflowInstance wf = workflowService.updateWorkflow(id, input);
        return wf == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(wf);
    }

    @GetMapping("/tasks/{userId}")
    public ResponseEntity<List<ApprovalTask>> tasksForUser(@PathVariable String userId) throws Exception {
        return ResponseEntity.ok(workflowService.getTasks(userId));
    }

    @PostMapping("/tasks/{taskId}/decision")
    public ResponseEntity<ApprovalTask> decideTask(
            @PathVariable String taskId,
            @RequestBody TaskActionInput input
    ) throws Exception {
        return ResponseEntity.ok(workflowService.decideTask(taskId, input));
    }
}
