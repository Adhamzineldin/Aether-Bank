import { forwardRef, type SelectHTMLAttributes } from 'react';
import { cn } from '@shared/utils/cn';

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  invalid?: boolean;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { className, invalid, children, ...rest },
  ref,
) {
  return (
    <select
      ref={ref}
      className={cn(
        'h-10 w-full rounded-lg border bg-card text-fg shadow-sm px-3 pr-9',
        'transition-colors focus:border-primary focus:ring-2 focus:ring-primary/20',
        invalid ? 'border-danger' : 'border-border',
        className,
      )}
      {...rest}
    >
      {children}
    </select>
  );
});

