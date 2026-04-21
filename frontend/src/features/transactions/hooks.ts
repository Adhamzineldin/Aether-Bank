import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useStubQuery, unavailableMutation } from '@lib/stub';
import type { TransferRequest } from '@veld/types';

export function useAccountTransactions(accountId: string, currency: string, _page = 0, _pageSize = 20) {
  // transaction-service has no REST controllers yet; return empty page.
  return useStubQuery<{ content: any[]; pageNumber: number; totalPages: number }>(
    ['txs', accountId, currency, _page],
    { content: [], pageNumber: 0, totalPages: 0 },
  );
}

export function useTransfer() {
  return useMutation({
    mutationFn: unavailableMutation<TransferRequest, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}
