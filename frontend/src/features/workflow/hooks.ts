import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useStubQuery, unavailableMutation } from '@lib/stub';
import { useAuthStore } from '@stores/authStore';
import type { TaskActionInput } from '@veld/types';

export const wfKeys = {
  myTasks: (userId: string) => ['workflow', 'tasks', userId] as const,
  one: (id: string) => ['workflow', id] as const,
};

export function useMyTasks() {
  const id = useAuthStore((s) => s.user?.id) || '';
  return useStubQuery<any[]>(wfKeys.myTasks(id), []);
}

export function useWorkflow(id: string | undefined) {
  return useStubQuery(wfKeys.one(id || ''));
}

export function useTaskAction(_taskId: string) {
  return useMutation({
    mutationFn: unavailableMutation<TaskActionInput, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}
