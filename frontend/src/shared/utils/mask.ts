export function maskAccount(accountNumber: string | undefined, visible = 4): string {
  if (!accountNumber) return '—';
  if (accountNumber.length <= visible) return accountNumber;
  return `•••• ${accountNumber.slice(-visible)}`;
}

export function maskCard(lastFour: string | undefined): string {
  return lastFour ? `•••• •••• •••• ${lastFour}` : '•••• •••• •••• ••••';
}

