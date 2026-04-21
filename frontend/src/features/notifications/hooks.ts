import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useStubQuery, unavailableMutation } from '@lib/stub';
import type { CreateTemplateInput, SendNotificationInput, UpdateTemplateInput } from '@veld/types';

export const notifKeys = {
  list: ['notifications'] as const,
  one: (id: string) => ['notifications', id] as const,
  templates: ['notification-templates'] as const,
  template: (id: string) => ['notification-templates', id] as const,
};

export function useNotifications() {
  return useStubQuery<unknown[]>(notifKeys.list, []);
}

export function useNotification(id: string | undefined) {
  return useStubQuery(notifKeys.one(id || ''));
}

export function useRetryNotification() {
  return useMutation({
    mutationFn: unavailableMutation<string, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useSendNotification() {
  return useMutation({
    mutationFn: unavailableMutation<SendNotificationInput, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useTemplates() {
  return useStubQuery<unknown[]>(notifKeys.templates, []);
}

export function useCreateTemplate() {
  return useMutation({
    mutationFn: unavailableMutation<CreateTemplateInput, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useUpdateTemplate(_id: string) {
  return useMutation({
    mutationFn: unavailableMutation<UpdateTemplateInput, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useDeactivateTemplate() {
  return useMutation({
    mutationFn: unavailableMutation<string, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}
