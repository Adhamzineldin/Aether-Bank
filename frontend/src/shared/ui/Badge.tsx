import type { ReactNode } from 'react';
import { cn } from '@shared/utils/cn';

export type BadgeTone = 'neutral' | 'primary' | 'success' | 'warning' | 'danger' | 'info';

const tones: Record<BadgeTone, string> = {
  neutral: 'bg-muted text-muted-fg',
  primary: 'bg-primary/10 text-primary',
  success: 'bg-success/10 text-success-600',
  warning: 'bg-warning/10 text-warning-600',
  danger: 'bg-danger/10 text-danger-600',
  info: 'bg-info/10 text-info-600',
};

export function Badge({
  children,
  tone = 'neutral',
  className,
}: {
  children: ReactNode;
  tone?: BadgeTone;
  className?: string;
}) {
  return (
    <span className={cn('inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium', tones[tone], className)}>
      {children}
    </span>
  );
}

