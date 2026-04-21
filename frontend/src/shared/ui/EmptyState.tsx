import type { ReactNode } from 'react';
import { cn } from '@shared/utils/cn';

interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description?: string;
  action?: ReactNode;
  className?: string;
}

export function EmptyState({ icon, title, description, action, className }: EmptyStateProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center text-center py-12 px-6', className)}>
      {icon && <div className="mb-4 grid h-12 w-12 place-items-center rounded-full bg-muted text-muted-fg">{icon}</div>}
      <h3 className="text-base font-semibold">{title}</h3>
      {description && <p className="mt-1 max-w-sm text-sm text-muted-fg">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}

