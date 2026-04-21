import { type ReactNode } from 'react';
import { ChevronLeft } from 'lucide-react';
import { Link } from 'react-router-dom';
import { cn } from '@shared/utils/cn';

interface Props {
  title: ReactNode;
  description?: ReactNode;
  actions?: ReactNode;
  back?: { to: string; label?: string };
  className?: string;
}

export function PageHeader({ title, description, actions, back, className }: Props) {
  return (
    <div className={cn('flex flex-col gap-3 pb-6 md:flex-row md:items-end md:justify-between', className)}>
      <div className="space-y-1">
        {back && (
          <Link to={back.to} className="inline-flex items-center gap-1 text-xs text-muted-fg hover:text-fg">
            <ChevronLeft className="h-3.5 w-3.5" /> {back.label || 'Back'}
          </Link>
        )}
        <h1 className="text-2xl font-bold tracking-tight">{title}</h1>
        {description && <p className="text-sm text-muted-fg max-w-2xl">{description}</p>}
      </div>
      {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
    </div>
  );
}

