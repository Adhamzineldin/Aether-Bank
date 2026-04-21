export const txKeys = {
  byAccount: (accountId: string, currency: string, page: number) =>
    ['transactions', accountId, currency, page] as const,
};

