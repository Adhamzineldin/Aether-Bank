import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { accountKeys } from '@features/accounts/keys';
import type { TransferRequest } from '@veld/types';
import { transactionsApi, type PaginatedTransactionsDto, type TransferResponseDto } from './api';
import { txKeys } from './keys';

/**
 * Paginated transaction history backed by the hand-written
 * `/transactions/history/{accountId}` query-param endpoint.
 */
export function useAccountTransactions(accountId: string, currency: string, page = 0, pageSize = 20) {
  return useQuery<PaginatedTransactionsDto>({
    queryKey: txKeys.byAccount(accountId, currency, page),
    enabled: !!accountId && !!currency,
    queryFn: () => transactionsApi.history(accountId, currency, page, pageSize),
  });
}

export function useTransfer() {
  const qc = useQueryClient();
  return useMutation<TransferResponseDto, Error, TransferRequest>({
    mutationFn: (payload) => transactionsApi.transfer(payload),
    onSuccess: (res) => {
      toast.success(
        res.referenceNumber
          ? `Transfer ${res.referenceNumber} sent`
          : 'Transfer sent',
      );
      qc.invalidateQueries({ queryKey: accountKeys.all });
      qc.invalidateQueries({ queryKey: ['transactions'] });
    },
    onError: (e) => toast.error(e.message),
  });
}
