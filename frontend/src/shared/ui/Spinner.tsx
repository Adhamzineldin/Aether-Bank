import { Loader2 } from 'lucide-react';
import { cn } from '@shared/utils/cn';

export function Spinner({ className }: { className?: string }) {
  return <Loader2 className={cn('h-5 w-5 animate-spin text-muted-fg', className)} />;
}

export function FullPageSpinner({ label }: { label?: string }) {
  return (
    <div className="flex h-full w-full flex-col items-center justify-center gap-3 py-16">
      <Spinner className="h-7 w-7 text-primary" />
      {label && <p className="text-sm text-muted-fg">{label}</p>}
    </div>
  );
}

