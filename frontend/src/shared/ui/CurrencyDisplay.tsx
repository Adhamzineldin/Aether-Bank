import { cn } from '@shared/utils/cn';
import { formatMoney } from '@shared/utils/format';

interface Props {
  amount: number | string | undefined | null;
  currency?: string;
  className?: string;
  signed?: boolean;
}

export function CurrencyDisplay({ amount, currency = 'USD', className, signed }: Props) {
  const n = amount === undefined || amount === null || amount === '' ? null : Number(amount);
  const positive = n !== null && n >= 0;
  const sign = signed && n !== null ? (positive ? '+' : '') : '';
  const tone = signed && n !== null ? (positive ? 'text-success-600' : 'text-danger-600') : '';
  return <span className={cn('font-mono tabular-nums', tone, className)}>{sign}{formatMoney(amount, currency)}</span>;
}

