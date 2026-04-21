import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useStubQuery, unavailableMutation } from '@lib/stub';
import type {
  MerchantPaymentRequest, RefundCardTransactionRequest, VoidCardTransactionRequest,
} from '@veld/types';

export const cardKeys = {
  one: (id: string) => ['cards', id] as const,
  txs: (id: string, page: number) => ['cards', id, 'txs', page] as const,
};

export function useCard(id: string | undefined) {
  return useStubQuery(cardKeys.one(id || ''));
}

export function useCardTransactions(id: string | undefined, page = 0, _pageSize = 20) {
  return useStubQuery<{ content: any[]; pageNumber: number; totalPages: number }>(
    cardKeys.txs(id || '', page),
    { content: [], pageNumber: 0, totalPages: 0 },
  );
}

export function useProcessMerchantPayment() {
  return useMutation({
    mutationFn: unavailableMutation<MerchantPaymentRequest, any>(),
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useRefundTransaction() {
  return useMutation({
    mutationFn: unavailableMutation<RefundCardTransactionRequest, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useVoidTransaction() {
  return useMutation({
    mutationFn: unavailableMutation<VoidCardTransactionRequest, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}
