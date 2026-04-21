import { forwardRef, type TextareaHTMLAttributes } from 'react';
import { cn } from '@shared/utils/cn';

export interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  invalid?: boolean;
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(function Textarea(
  { className, invalid, ...rest },
  ref,
) {
  return (
    <textarea
      ref={ref}
      className={cn(
        'min-h-[88px] w-full rounded-lg border bg-card text-fg shadow-sm px-3 py-2 placeholder:text-muted-fg',
        'transition-colors focus:border-primary focus:ring-2 focus:ring-primary/20',
        invalid ? 'border-danger' : 'border-border',
        className,
      )}
      {...rest}
    />
  );
});

