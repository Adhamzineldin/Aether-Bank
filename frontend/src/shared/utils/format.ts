export function formatMoney(
  amount: number | string | undefined | null,
  currency = 'USD',
  locale = 'en-US',
): string {
  if (amount === undefined || amount === null || amount === '') return '—';
  const n = typeof amount === 'string' ? Number(amount) : amount;
  if (Number.isNaN(n)) return '—';
  return new Intl.NumberFormat(locale, { style: 'currency', currency }).format(n);
}

export function formatNumber(value: number | string | undefined, fractionDigits = 2): string {
  if (value === undefined || value === null || value === '') return '—';
  const n = typeof value === 'string' ? Number(value) : value;
  if (Number.isNaN(n)) return '—';
  return new Intl.NumberFormat('en-US', { maximumFractionDigits: fractionDigits, minimumFractionDigits: 0 }).format(n);
}

export function formatPercent(value: number | string | undefined, fractionDigits = 2): string {
  if (value === undefined || value === null) return '—';
  const n = typeof value === 'string' ? Number(value) : value;
  if (Number.isNaN(n)) return '—';
  return `${n >= 0 ? '+' : ''}${n.toFixed(fractionDigits)}%`;
}

