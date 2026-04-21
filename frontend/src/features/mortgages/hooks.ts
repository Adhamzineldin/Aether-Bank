import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useAuthStore } from '@stores/authStore';
import type { MortgageApplication } from '@veld/types';
import { mortgagesApi } from './api';

export const mortgageKeys = {
  all: ['mortgages'] as const,
  byCustomer: (id: string) => ['mortgages', 'customer', id] as const,
  one: (id: string) => ['mortgages', id] as const,
  schedule: (id: string) => ['mortgages', id, 'schedule'] as const,
};

export function useMyMortgages() {
  const customerId = useAuthStore((s) => s.user?.id);
  return useQuery({
    queryKey: mortgageKeys.byCustomer(customerId || ''),
    enabled: !!customerId,
    queryFn: () => mortgagesApi.listByCustomer(customerId as string),
  });
}

export function useMortgage(id: string | undefined) {
  return useQuery({
    queryKey: mortgageKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => mortgagesApi.get(id as string),
  });
}

export function useMortgageSchedule(id: string | undefined) {
  return useQuery({
    queryKey: mortgageKeys.schedule(id || ''),
    enabled: !!id,
    queryFn: () => mortgagesApi.schedule(id as string),
  });
}

export function useApplyMortgage() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: MortgageApplication) => mortgagesApi.submitApplication(payload),
    onSuccess: () => {
      toast.success('Mortgage application submitted');
      qc.invalidateQueries({ queryKey: mortgageKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}
