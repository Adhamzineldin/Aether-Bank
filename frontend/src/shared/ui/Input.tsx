import { forwardRef, type InputHTMLAttributes } from 'react';
import { cn } from '@shared/utils/cn';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  invalid?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { className, invalid, leftIcon, rightIcon, ...rest },
  ref,
) {
  if (leftIcon || rightIcon) {
    return (
      <div className="relative w-full">
        {leftIcon && (
          <span className="pointer-events-none absolute inset-y-0 left-0 grid w-10 place-items-center text-muted-fg">
            {leftIcon}
          </span>
        )}
        <input
          ref={ref}
          className={cn(
            'h-10 w-full rounded-lg border bg-card text-fg shadow-sm placeholder:text-muted-fg',
            'transition-colors focus:border-primary focus:ring-2 focus:ring-primary/20',
            invalid ? 'border-danger' : 'border-border',
            leftIcon ? 'pl-10' : 'pl-3',
            rightIcon ? 'pr-10' : 'pr-3',
            className,
          )}
          {...rest}
        />
        {rightIcon && (
          <span className="pointer-events-none absolute inset-y-0 right-0 grid w-10 place-items-center text-muted-fg">
            {rightIcon}
          </span>
        )}
      </div>
    );
  }
  return (
    <input
      ref={ref}
      className={cn(
        'h-10 w-full rounded-lg border bg-card text-fg shadow-sm placeholder:text-muted-fg px-3',
        'transition-colors focus:border-primary focus:ring-2 focus:ring-primary/20',
        invalid ? 'border-danger' : 'border-border',
        className,
      )}
      {...rest}
    />
  );
});

