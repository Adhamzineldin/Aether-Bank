import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useVeld } from '@shared/hooks/useVeld';
import { useAuthStore } from '@stores/authStore';
import type { MortgageApplication } from '@veld/types';

export const mortgageKeys = {
  byCustomer: (id: string) => ['mortgages', 'customer', id] as const,
  one: (id: string) => ['mortgages', id] as const,
  schedule: (id: string) => ['mortgages', id, 'schedule'] as const,
};

export function useMyMortgages() {
  const veld = useVeld();
  const customerId = useAuthStore((s) => s.user?.id);
  return useQuery({
    queryKey: mortgageKeys.byCustomer(customerId || ''),
    enabled: !!customerId,
    queryFn: () => veld.mortgage.listCustomerMortgages(customerId as string),
  });
}

export function useMortgage(id: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: mortgageKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => veld.mortgage.getMortgage(id as string),
  });
}

export function useMortgageSchedule(id: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: mortgageKeys.schedule(id || ''),
    enabled: !!id,
    queryFn: () => veld.mortgage.getMortgageSchedule(id as string),
  });
}

export function useApplyMortgage() {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: MortgageApplication) => veld.mortgage.submitMortgageApplication(input),
    onSuccess: () => {
      toast.success('Mortgage application submitted');
      qc.invalidateQueries({ queryKey: ['mortgages'] });
    },
  });
}

