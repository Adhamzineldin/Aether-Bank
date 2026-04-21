import { http } from '@lib/axios';
import type {
  ApprovalTask,
  CreateWorkflowInput,
  TaskActionInput,
  UpdateWorkflowInput,
  WorkflowInstance,
} from '@veld/types';

const BASE = '/api/workflow';

export const workflowApi = {
  list: () => http.get<WorkflowInstance[]>(BASE).then((r) => r.data),
  get: (id: string) => http.get<WorkflowInstance>(`${BASE}/${id}`).then((r) => r.data),
  create: (payload: CreateWorkflowInput) =>
    http.post<WorkflowInstance>(BASE, payload).then((r) => r.data),
  update: (id: string, payload: UpdateWorkflowInput) =>
    http.put<WorkflowInstance>(`${BASE}/${id}`, payload).then((r) => r.data),

  /** Tasks for a given user (id is the assignee UUID). */
  tasksFor: (userId: string) =>
    http.get<ApprovalTask[]>(`${BASE}/tasks/${userId}`).then((r) => r.data),

  decideTask: (taskId: string, payload: TaskActionInput) =>
    http
      .post<ApprovalTask>(`${BASE}/tasks/${taskId}/decision`, payload)
      .then((r) => r.data),
};
