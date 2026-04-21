import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useVeld } from '@shared/hooks/useVeld';
import { txKeys } from './keys';
import type { TransferRequest } from '@veld/types';

export function useAccountTransactions(accountId: string, currency: string, page = 0, pageSize = 20) {
  const veld = useVeld();
  return useQuery({
    queryKey: txKeys.byAccount(accountId, currency, page),
    enabled: !!accountId && !!currency,
    queryFn: () => veld.transaction.getAccountTransactions(accountId, { currency, page, pageSize } as any),
  });
}

export function useTransfer() {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: TransferRequest) => veld.transaction.transfer(input),
    onSuccess: () => {
      toast.success('Transfer initiated');
      qc.invalidateQueries({ queryKey: ['transactions'] });
      qc.invalidateQueries({ queryKey: ['accounts'] });
    },
  });
}

