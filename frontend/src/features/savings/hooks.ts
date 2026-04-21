import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useVeld } from '@shared/hooks/useVeld';
import type { CertificateApplication } from '@veld/types';

export function useApplyCertificate() {
  const veld = useVeld();
  return useMutation({
    mutationFn: (input: CertificateApplication) => veld.certificate.certificateSubmit(input),
    onSuccess: () => toast.success('Certificate application submitted'),
  });
}

