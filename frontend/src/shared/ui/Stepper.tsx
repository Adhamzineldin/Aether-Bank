import { Check } from 'lucide-react';
import { cn } from '@shared/utils/cn';

interface Step {
  id: string;
  label: string;
}

export function Stepper({ steps, currentIndex }: { steps: Step[]; currentIndex: number }) {
  return (
    <ol className="flex items-center gap-3">
      {steps.map((s, i) => {
        const done = i < currentIndex;
        const active = i === currentIndex;
        return (
          <li key={s.id} className="flex items-center gap-2">
            <span
              className={cn(
                'grid h-7 w-7 place-items-center rounded-full text-xs font-semibold border',
                done && 'bg-primary text-primary-fg border-primary',
                active && 'border-primary text-primary',
                !done && !active && 'border-border text-muted-fg',
              )}
            >
              {done ? <Check className="h-4 w-4" /> : i + 1}
            </span>
            <span className={cn('text-sm', active ? 'font-semibold' : 'text-muted-fg')}>{s.label}</span>
            {i < steps.length - 1 && <span className="mx-2 h-px w-8 bg-border" />}
          </li>
        );
      })}
    </ol>
  );
}

