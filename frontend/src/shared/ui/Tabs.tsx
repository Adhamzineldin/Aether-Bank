import type { ReactNode } from 'react';
import { cn } from '@shared/utils/cn';

interface TabItem {
  id: string;
  label: ReactNode;
}

export function Tabs({
  items,
  activeId,
  onChange,
  className,
}: {
  items: TabItem[];
  activeId: string;
  onChange: (id: string) => void;
  className?: string;
}) {
  return (
    <div className={cn('inline-flex rounded-lg bg-muted p-1 gap-1', className)}>
      {items.map((it) => (
        <button
          key={it.id}
          onClick={() => onChange(it.id)}
          className={cn(
            'rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
            activeId === it.id ? 'bg-card text-fg shadow-sm' : 'text-muted-fg hover:text-fg',
          )}
        >
          {it.label}
        </button>
      ))}
    </div>
  );
}

