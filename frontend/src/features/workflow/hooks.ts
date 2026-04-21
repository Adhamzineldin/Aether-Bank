import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useVeld } from '@shared/hooks/useVeld';
import { useAuthStore } from '@stores/authStore';
import type { TaskActionInput } from '@veld/types';

export const wfKeys = {
  myTasks: (userId: string) => ['workflow', 'tasks', userId] as const,
  one: (id: string) => ['workflow', id] as const,
};

export function useMyTasks() {
  const veld = useVeld();
  const id = useAuthStore((s) => s.user?.id) || '';
  return useQuery({
    queryKey: wfKeys.myTasks(id),
    enabled: !!id,
    queryFn: () => veld.workflow.getTasks(id),
  });
}

export function useWorkflow(id: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: wfKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => veld.workflow.getWorkflow(id as string),
  });
}

export function useTaskAction(taskId: string) {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: TaskActionInput) => veld.workflow.decideTask(taskId, input),
    onSuccess: () => {
      toast.success('Decision recorded');
      qc.invalidateQueries({ queryKey: ['workflow'] });
    },
  });
}


