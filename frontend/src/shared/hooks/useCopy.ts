import { useCallback, useState } from 'react';
import { toast } from 'sonner';

export function useCopy(timeout = 1500) {
  const [copied, setCopied] = useState(false);
  const copy = useCallback(
    async (text: string, label = 'Copied') => {
      try {
        await navigator.clipboard.writeText(text);
        setCopied(true);
        toast.success(label);
        setTimeout(() => setCopied(false), timeout);
      } catch {
        toast.error('Could not copy');
      }
    },
    [timeout],
  );
  return { copied, copy };
}

