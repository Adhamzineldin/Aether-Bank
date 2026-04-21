import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { unavailableMutation } from '@lib/stub';
import type { CertificateApplication } from '@veld/types';

export function useApplyCertificate() {
  return useMutation({
    mutationFn: unavailableMutation<CertificateApplication, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}
