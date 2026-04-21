import type { ReactNode } from 'react';
import { cn } from '@shared/utils/cn';
import { Card } from './Card';

interface StatProps {
  label: string;
  value: ReactNode;
  hint?: ReactNode;
  icon?: ReactNode;
  trend?: 'up' | 'down' | 'neutral';
  className?: string;
}

export function Stat({ label, value, hint, icon, trend = 'neutral', className }: StatProps) {
  const trendColor = trend === 'up' ? 'text-success-600' : trend === 'down' ? 'text-danger-600' : 'text-muted-fg';
  return (
    <Card className={cn('p-5', className)}>
      <div className="flex items-start justify-between">
        <p className="text-xs font-medium uppercase tracking-wide text-muted-fg">{label}</p>
        {icon && <span className="text-muted-fg">{icon}</span>}
      </div>
      <p className="mt-2 text-2xl font-bold tracking-tight">{value}</p>
      {hint && <p className={cn('mt-1 text-xs', trendColor)}>{hint}</p>}
    </Card>
  );
}

