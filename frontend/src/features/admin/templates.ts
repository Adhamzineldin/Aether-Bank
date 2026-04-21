import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { http } from '@lib/axios';

export interface WorkflowStep {
  id?: string;
  step: number;
  role: string;
  action: string;
}

export interface WorkflowTemplate {
  id: string;
  entityType: string;
  steps: WorkflowStep[];
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface TemplatePayload {
  entityType: string;
  steps: Array<{ id?: string; role: string; action: string }>;
}

const BASE = '/api/workflow/templates';

export const workflowTemplatesApi = {
  list: () => http.get<WorkflowTemplate[]>(BASE).then((r) => r.data),
  get: (id: string) => http.get<WorkflowTemplate>(`${BASE}/${id}`).then((r) => r.data),
  create: (payload: TemplatePayload) =>
    http.post<WorkflowTemplate>(BASE, payload).then((r) => r.data),
  update: (id: string, payload: TemplatePayload) =>
    http.put<WorkflowTemplate>(`${BASE}/${id}`, payload).then((r) => r.data),
  remove: (id: string) => http.delete<void>(`${BASE}/${id}`).then((r) => r.data),
};

export const wfTemplateKeys = {
  all: ['workflow-templates'] as const,
  list: ['workflow-templates', 'list'] as const,
  one: (id: string) => ['workflow-templates', id] as const,
};

export function useWorkflowTemplates() {
  return useQuery({ queryKey: wfTemplateKeys.list, queryFn: workflowTemplatesApi.list });
}

export function useCreateWorkflowTemplate() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: TemplatePayload) => workflowTemplatesApi.create(payload),
    onSuccess: () => {
      toast.success('Template created');
      qc.invalidateQueries({ queryKey: wfTemplateKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useUpdateWorkflowTemplate(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: TemplatePayload) => workflowTemplatesApi.update(id, payload),
    onSuccess: () => {
      toast.success('Template saved');
      qc.invalidateQueries({ queryKey: wfTemplateKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useDeleteWorkflowTemplate() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => workflowTemplatesApi.remove(id),
    onSuccess: () => {
      toast.success('Template deleted');
      qc.invalidateQueries({ queryKey: wfTemplateKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}
