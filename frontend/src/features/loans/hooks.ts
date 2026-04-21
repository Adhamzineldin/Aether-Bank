import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useAuthStore } from '@stores/authStore';
import type { LoanApplication } from '@veld/types';
import { loansApi } from './api';

export const loanKeys = {
  all: ['loans'] as const,
  products: ['loans', 'products'] as const,
  byCustomer: (id: string) => ['loans', 'customer', id] as const,
  one: (id: string) => ['loans', id] as const,
};

export function useLoanProducts() {
  return useQuery({
    queryKey: loanKeys.products,
    queryFn: loansApi.listProducts,
    staleTime: 60_000,
  });
}

export function useMyLoans() {
  const customerId = useAuthStore((s) => s.user?.id);
  return useQuery({
    queryKey: loanKeys.byCustomer(customerId || ''),
    enabled: !!customerId,
    queryFn: () => loansApi.listByCustomer(customerId as string),
  });
}

export function useLoan(id: string | undefined) {
  return useQuery({
    queryKey: loanKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => loansApi.get(id as string),
  });
}

export function useApplyLoan() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: LoanApplication) => loansApi.submitApplication(payload),
    onSuccess: () => {
      toast.success('Loan application submitted');
      qc.invalidateQueries({ queryKey: loanKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}
