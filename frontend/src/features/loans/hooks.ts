import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useVeld } from '@shared/hooks/useVeld';
import type { LoanApplication } from '@veld/types';

export function useApplyLoan() {
  const veld = useVeld();
  return useMutation({
    mutationFn: (input: LoanApplication) => veld.loan.loanSubmit(input),
    onSuccess: () => toast.success('Application submitted'),
  });
}

