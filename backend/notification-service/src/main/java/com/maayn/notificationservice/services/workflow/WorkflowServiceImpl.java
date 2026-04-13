package com.maayn.notificationservice.services.workflow;

import maayn.veld.generated.models.*;
import maayn.veld.generated.services.IWorkflowService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowServiceImpl implements IWorkflowService {

    @Override
    public WorkflowInstance createWorkflow(CreateWorkflowInput input) throws Exception {
        return null;
    }

    @Override
    public WorkflowInstance updateWorkflow(String id, UpdateWorkflowInput input) throws Exception {
        return null;
    }

    @Override
    public WorkflowInstance getWorkflow(String id) throws Exception {
        return null;
    }

    @Override
    public List<WorkflowInstance> getWorkflows() throws Exception {
        return List.of();
    }

    @Override
    public List<ApprovalTask> getTasks(String id) throws Exception {
        return List.of();
    }

    @Override
    public ApprovalTask decideTask(String taskId, TaskActionInput input) throws Exception {
        return null;
    }
}
