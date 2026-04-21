import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useStubQuery, unavailableMutation } from '@lib/stub';
import { useAuthStore } from '@stores/authStore';
import type { MortgageApplication } from '@veld/types';

export const mortgageKeys = {
  byCustomer: (id: string) => ['mortgages', 'customer', id] as const,
  one: (id: string) => ['mortgages', id] as const,
  schedule: (id: string) => ['mortgages', id, 'schedule'] as const,
};

export function useMyMortgages() {
  const customerId = useAuthStore((s) => s.user?.id);
  return useStubQuery<any[]>(mortgageKeys.byCustomer(customerId || ''), []);
}

export function useMortgage(id: string | undefined) {
  return useStubQuery(mortgageKeys.one(id || ''));
}

export function useMortgageSchedule(id: string | undefined) {
  return useStubQuery<any[]>(mortgageKeys.schedule(id || ''), []);
}

export function useApplyMortgage() {
  return useMutation({
    mutationFn: unavailableMutation<MortgageApplication, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}
