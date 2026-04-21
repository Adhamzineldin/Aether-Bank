import { Copy, Check } from 'lucide-react';
import { useCopy } from '@shared/hooks/useCopy';
import { cn } from '@shared/utils/cn';

export function CopyButton({ value, className }: { value: string; className?: string }) {
  const { copied, copy } = useCopy();
  return (
    <button
      onClick={() => copy(value)}
      className={cn('inline-flex items-center gap-1 rounded p-1 text-muted-fg hover:text-fg hover:bg-muted', className)}
      aria-label="Copy"
    >
      {copied ? <Check className="h-3.5 w-3.5 text-success-600" /> : <Copy className="h-3.5 w-3.5" />}
    </button>
  );
}

