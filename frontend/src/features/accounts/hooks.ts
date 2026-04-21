import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useAuthStore } from '@stores/authStore';
import { accountKeys } from './keys';
import { accountsApi, type OpenAccountInput, type CloseAccountInput, type UpdateAccountStatusInput } from './api';

export function useMyAccounts() {
  const customerId = useAuthStore((s) => s.user?.id);
  return useQuery({
    queryKey: accountKeys.byCustomer(customerId || ''),
    enabled: !!customerId,
    queryFn: () => accountsApi.listByCustomer(customerId as string),
  });
}

export function useAccount(id: string | undefined) {
  return useQuery({
    queryKey: accountKeys.one(id || ''),
    enabled: !!id,
    queryFn: () => accountsApi.get(id as string),
  });
}

export function useOpenAccount() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: OpenAccountInput) => accountsApi.open(input),
    onSuccess: () => {
      toast.success('Account opened');
      qc.invalidateQueries({ queryKey: accountKeys.all });
    },
  });
}

export function useCloseAccount(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CloseAccountInput) => accountsApi.close(id, input),
    onSuccess: () => {
      toast.success('Account closed');
      qc.invalidateQueries({ queryKey: accountKeys.all });
    },
  });
}

export function useUpdateAccountStatus(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: UpdateAccountStatusInput) => accountsApi.updateStatus(id, input),
    onSuccess: () => {
      toast.success('Status updated');
      qc.invalidateQueries({ queryKey: accountKeys.one(id) });
    },
  });
}
