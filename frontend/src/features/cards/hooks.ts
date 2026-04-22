import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useAuthStore } from '@stores/authStore';
import type {
  MerchantPaymentRequest,
  RefundCardTransactionRequest,
  VoidCardTransactionRequest,
} from '@veld/types';
import { cardsApi, type IssueCardRequest } from './api';

export const cardKeys = {
  all: ['cards'] as const,
  byCustomer: (id: string) => ['cards', 'customer', id] as const,
  one: (id: string) => ['cards', id] as const,
  txs: (id: string, page: number) => ['cards', id, 'txs', page] as const,
};

export function useMyCards() {
  const customerId = useAuthStore((s) => s.user?.id);
  return useQuery({
    queryKey: cardKeys.byCustomer(customerId || ''),
    enabled: !!customerId,
    queryFn: () => cardsApi.listByCustomer(customerId as string),
  });
}

export function useCard(id: string | undefined) {
  return useQuery({
    queryKey: cardKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => cardsApi.get(id as string),
  });
}

export function useIssueCard() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: IssueCardRequest) => cardsApi.issue(payload),
    onSuccess: () => {
      toast.success('Card issued');
      qc.invalidateQueries({ queryKey: cardKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}

/**
 * Card transaction history. card-service exposes a browser-friendly
 * GET endpoint with query-string filters at /api/card/{cardId}/transactions
 * — that's what we hit here.
 */
export function useCardTransactions(id: string | undefined, page = 0, pageSize = 20) {
  return useQuery({
    queryKey: cardKeys.txs(id || '', page),
    queryFn: () => cardsApi.listTransactions(id as string, { page, pageSize }),
    enabled: Boolean(id),
  });
}

export function useProcessMerchantPayment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: MerchantPaymentRequest) => cardsApi.processMerchantPayment(payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: cardKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useRefundTransaction() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: RefundCardTransactionRequest) => cardsApi.refundTransaction(payload),
    onSuccess: () => {
      toast.success('Refund processed');
      qc.invalidateQueries({ queryKey: cardKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useVoidTransaction() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: VoidCardTransactionRequest) => cardsApi.voidTransaction(payload),
    onSuccess: () => {
      toast.success('Transaction voided');
      qc.invalidateQueries({ queryKey: cardKeys.all });
    },
    onError: (e: Error) => toast.error(e.message),
  });
}
