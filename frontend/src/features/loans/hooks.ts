import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { unavailableMutation } from '@lib/stub';
import type { LoanApplication } from '@veld/types';

export function useApplyLoan() {
  return useMutation({
    mutationFn: unavailableMutation<LoanApplication, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}
