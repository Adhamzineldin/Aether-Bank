import type { HTMLAttributes, ReactNode } from 'react';
import { cn } from '@shared/utils/cn';

export function Table({ className, ...rest }: HTMLAttributes<HTMLTableElement>) {
  return (
    <div className="overflow-x-auto">
      <table className={cn('w-full text-sm', className)} {...rest} />
    </div>
  );
}
export function THead({ children }: { children: ReactNode }) {
  return <thead className="border-b border-border bg-muted/40 text-xs uppercase tracking-wide text-muted-fg">{children}</thead>;
}
export function TBody({ children }: { children: ReactNode }) {
  return <tbody className="divide-y divide-border">{children}</tbody>;
}
export function TR({ className, ...rest }: HTMLAttributes<HTMLTableRowElement>) {
  return <tr className={cn('hover:bg-muted/30 transition-colors', className)} {...rest} />;
}
export function TH({ className, children }: { className?: string; children: ReactNode }) {
  return <th className={cn('px-4 py-3 text-left font-medium', className)}>{children}</th>;
}
export function TD({ className, children, ...rest }: HTMLAttributes<HTMLTableCellElement>) {
  return <td className={cn('px-4 py-3 align-middle', className)} {...rest}>{children}</td>;
}

