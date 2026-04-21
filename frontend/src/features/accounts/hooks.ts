import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useVeld } from '@shared/hooks/useVeld';
import { useAuthStore } from '@stores/authStore';
import { accountKeys } from './keys';
import type { OpenAccountRequest, CloseAccountRequest, UpdateAccountStatusRequest } from '@veld/types';

export function useMyAccounts() {
  const veld = useVeld();
  const customerId = useAuthStore((s) => s.user?.id);
  return useQuery({
    queryKey: accountKeys.byCustomer(customerId || ''),
    enabled: !!customerId,
    queryFn: () => veld.account.listCustomerAccounts(customerId as string),
  });
}

export function useAccount(id: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: accountKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => veld.account.getAccount(id as string),
  });
}

export function useAccountBalance(id: string | undefined, currency: string) {
  const veld = useVeld();
  return useQuery({
    queryKey: accountKeys.balance(id || '', currency),
    enabled: !!id && !!currency,
    queryFn: () => veld.ledger.getAccountBalance(id as string, currency),
  });
}

export function useOpenAccount() {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: OpenAccountRequest) => veld.account.openAccount(input),
    onSuccess: () => {
      toast.success('Account opened');
      qc.invalidateQueries({ queryKey: accountKeys.all });
    },
  });
}

export function useCloseAccount(id: string) {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CloseAccountRequest) => veld.account.closeAccount(id, input),
    onSuccess: () => {
      toast.success('Account closed');
      qc.invalidateQueries({ queryKey: accountKeys.all });
    },
  });
}

export function useUpdateAccountStatus(id: string) {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: UpdateAccountStatusRequest) => veld.account.updateAccountStatus(id, input),
    onSuccess: () => {
      toast.success('Status updated');
      qc.invalidateQueries({ queryKey: accountKeys.one(id) });
    },
  });
}

