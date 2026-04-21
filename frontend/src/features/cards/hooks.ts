import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useVeld } from '@shared/hooks/useVeld';
import type {
  MerchantPaymentRequest, RefundCardTransactionRequest, VoidCardTransactionRequest,
  GetCardTransactionsRequest,
} from '@veld/types';

export const cardKeys = {
  one: (id: string) => ['cards', id] as const,
  txs: (id: string, page: number) => ['cards', id, 'txs', page] as const,
};

export function useCard(id: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: cardKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => veld.card.getCardDetails(id as string),
  });
}

export function useCardTransactions(id: string | undefined, page = 0, pageSize = 20) {
  const veld = useVeld();
  return useQuery({
    queryKey: cardKeys.txs(id || '', page),
    enabled: !!id,
    queryFn: () =>
      veld.card.getCardTransactions(id as string, { page, pageSize } as GetCardTransactionsRequest),
  });
}

export function useProcessMerchantPayment() {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: MerchantPaymentRequest) => veld.card.processMerchantPayment(input),
    onSuccess: () => {
      toast.success('Payment processed');
      qc.invalidateQueries({ queryKey: ['cards'] });
    },
  });
}

export function useRefundTransaction() {
  const veld = useVeld();
  return useMutation({
    mutationFn: (input: RefundCardTransactionRequest) => veld.card.refundTransaction(input),
    onSuccess: () => toast.success('Refund issued'),
  });
}

export function useVoidTransaction() {
  const veld = useVeld();
  return useMutation({
    mutationFn: (input: VoidCardTransactionRequest) => veld.card.voidTransaction(input),
    onSuccess: () => toast.success('Transaction voided'),
  });
}

