import { Toaster } from 'sonner';
import { useUIStore } from '@stores/uiStore';

export function ToastProvider() {
  const theme = useUIStore((s) => s.theme);
  return (
    <Toaster
      richColors
      closeButton
      position="top-right"
      theme={theme}
      toastOptions={{ classNames: { toast: 'rounded-lg border border-border' } }}
    />
  );
}

