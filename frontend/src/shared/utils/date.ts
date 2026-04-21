import { format, formatDistanceToNow, parseISO } from 'date-fns';

export function formatDate(value: string | Date | undefined, pattern = 'MMM d, yyyy'): string {
  if (!value) return '—';
  const d = typeof value === 'string' ? parseISO(value) : value;
  return Number.isNaN(d.getTime()) ? '—' : format(d, pattern);
}

export function formatDateTime(value: string | Date | undefined): string {
  return formatDate(value, 'MMM d, yyyy · HH:mm');
}

export function fromNow(value: string | Date | undefined): string {
  if (!value) return '—';
  const d = typeof value === 'string' ? parseISO(value) : value;
  return Number.isNaN(d.getTime()) ? '—' : formatDistanceToNow(d, { addSuffix: true });
}

