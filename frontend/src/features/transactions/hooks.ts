import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useStubQuery } from '@lib/stub';
import { accountKeys } from '@features/accounts/keys';
import type { TransferRequest } from '@veld/types';
import { transactionsApi, type TransferResponseDto } from './api';

/**
 * Transaction history endpoint is declared by Veld as `GET` with a
 * `@RequestBody` which browsers can't reliably send — keep this stubbed
 * until the spec is fixed to use query params. Accounts list + balance are
 * unaffected.
 */
export function useAccountTransactions(accountId: string, currency: string, _page = 0, _pageSize = 20) {
  return useStubQuery<{ content: any[]; pageNumber: number; totalPages: number }>(
    ['txs', accountId, currency, _page],
    { content: [], pageNumber: 0, totalPages: 0 },
  );
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
    },
    onError: (e) => toast.error(e.message),
  });
}
