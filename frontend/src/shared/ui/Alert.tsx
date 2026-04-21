import type { ReactNode } from 'react';
import { AlertCircle, CheckCircle2, Info, AlertTriangle } from 'lucide-react';
import { cn } from '@shared/utils/cn';

type Tone = 'info' | 'success' | 'warning' | 'danger';

const toneMap: Record<Tone, { wrap: string; icon: ReactNode }> = {
  info: { wrap: 'border-info/30 bg-info/5 text-info-600', icon: <Info className="h-4 w-4" /> },
  success: { wrap: 'border-success/30 bg-success/5 text-success-600', icon: <CheckCircle2 className="h-4 w-4" /> },
  warning: { wrap: 'border-warning/30 bg-warning/5 text-warning-600', icon: <AlertTriangle className="h-4 w-4" /> },
  danger: { wrap: 'border-danger/30 bg-danger/5 text-danger-600', icon: <AlertCircle className="h-4 w-4" /> },
};

export function Alert({ tone = 'info', title, children, className }: { tone?: Tone; title?: string; children?: ReactNode; className?: string }) {
  const t = toneMap[tone];
  return (
    <div className={cn('flex gap-3 rounded-lg border p-3', t.wrap, className)}>
      <span className="mt-0.5 shrink-0">{t.icon}</span>
      <div className="text-sm">
        {title && <p className="font-semibold">{title}</p>}
        {children && <div className="text-fg/90">{children}</div>}
      </div>
    </div>
  );
}

