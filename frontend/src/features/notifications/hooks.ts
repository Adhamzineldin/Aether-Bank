import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useVeld } from '@shared/hooks/useVeld';
import type { CreateTemplateInput, SendNotificationInput, UpdateTemplateInput } from '@veld/types';

export const notifKeys = {
  list: ['notifications'] as const,
  one: (id: string) => ['notifications', id] as const,
  templates: ['notification-templates'] as const,
  template: (id: string) => ['notification-templates', id] as const,
};

export function useNotifications() {
  const veld = useVeld();
  return useQuery({ queryKey: notifKeys.list, queryFn: () => veld.notification.listNotifications() });
}

export function useNotification(id: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: notifKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => veld.notification.getNotification(id as string),
  });
}

export function useRetryNotification() {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => veld.notification.retryNotification(id),
    onSuccess: () => { toast.success('Notification re-queued'); qc.invalidateQueries({ queryKey: notifKeys.list }); },
  });
}

export function useSendNotification() {
  const veld = useVeld();
  return useMutation({
    mutationFn: (input: SendNotificationInput) => veld.notification.sendNotification(input),
    onSuccess: () => toast.success('Notification sent'),
  });
}

export function useTemplates() {
  const veld = useVeld();
  return useQuery({ queryKey: notifKeys.templates, queryFn: () => veld.notification.listTemplates() });
}

export function useCreateTemplate() {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateTemplateInput) => veld.notification.createTemplate(input),
    onSuccess: () => { toast.success('Template created'); qc.invalidateQueries({ queryKey: notifKeys.templates }); },
  });
}

export function useUpdateTemplate(id: string) {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: UpdateTemplateInput) => veld.notification.updateTemplate(id, input),
    onSuccess: () => { toast.success('Template updated'); qc.invalidateQueries({ queryKey: notifKeys.templates }); },
  });
}

export function useDeactivateTemplate() {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => veld.notification.deactivateTemplate(id),
    onSuccess: () => { toast.success('Template deactivated'); qc.invalidateQueries({ queryKey: notifKeys.templates }); },
  });
}

