export const accountKeys = {
  all: ['accounts'] as const,
  byCustomer: (customerId: string) => ['accounts', 'customer', customerId] as const,
  one: (id: string) => ['accounts', 'one', id] as const,
  balance: (id: string, currency: string) => ['accounts', 'balance', id, currency] as const,
};

