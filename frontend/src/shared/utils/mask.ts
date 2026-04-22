export function maskAccount(accountNumber: string | undefined, visible = 4): string {
  if (!accountNumber) return '—';
  if (accountNumber.length <= visible) return accountNumber;
  return `•••• ${accountNumber.slice(-visible)}`;
}

export function maskCard(lastFour: string | undefined): string {
  return lastFour ? `•••• •••• •••• ${lastFour}` : '•••• •••• •••• ••••';
}

/** Groups 12–19 digit PAN as 4-digit blocks for display (digits only in → out). */
export function formatPanGroups(panDigits: string): string {
  const d = panDigits.replace(/\D/g, '');
  if (!d) return '';
  return d.replace(/(.{4})/g, '$1 ').trim();
}

