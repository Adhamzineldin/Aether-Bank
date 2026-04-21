import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useAuthStore } from '@stores/authStore';
import type { TaskActionInput } from '@veld/types';
import { workflowApi } from './api';

export const wfKeys = {
  all: ['workflow'] as const,
  list: ['workflow', 'list'] as const,
  myTasks: (userId: string) => ['workflow', 'tasks', userId] as const,
  one: (id: string) => ['workflow', id] as const,
};

export function useWorkflows() {
  return useQuery({ queryKey: wfKeys.list, queryFn: workflowApi.list });
}

export function useMyTasks() {
  const id = useAuthStore((s) => s.user?.id) || '';
  return useQuery({
    queryKey: wfKeys.myTasks(id),
    enabled: !!id,
    queryFn: () => workflowApi.tasksFor(id),
  });
}

export function useWorkflow(id: string | undefined) {
  return useQuery({
    queryKey: wfKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => workflowApi.get(id as string),
  });
}

export function useTaskAction(taskId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: TaskActionInput) => workflowApi.decideTask(taskId, payload),
    onSuccess: () => {
      toast.success('Decision recorded');
      qc.invalidateQueries({ queryKey: wfKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}
