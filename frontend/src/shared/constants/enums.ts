export const CURRENCIES = ['USD', 'EUR', 'GBP', 'EGP', 'AED', 'SAR', 'JPY', 'CAD'] as const;
export const ACCOUNT_TYPES = ['CHECKING', 'SAVINGS', 'INVESTMENT'] as const;
export const ACCOUNT_STATUSES = ['PENDING', 'ACTIVE', 'FROZEN', 'CLOSED'] as const;
export const ROLES = ['CUSTOMER', 'EMPLOYEE', 'ADMIN'] as const;
export const LOAN_TYPES = ['MORTGAGE', 'AUTO', 'PERSONAL', 'STUDENT', 'BUSINESS', 'CREDIT_CARD', 'PAYDAY', 'BRIDGE', 'LINE_OF_CREDIT'] as const;
export const EMPLOYMENT_STATUSES = ['EMPLOYED', 'SELF_EMPLOYED', 'UNEMPLOYED', 'STUDENT', 'RETIRED'] as const;
export const ASSET_TYPES = ['STOCK', 'BOND', 'ETF', 'MUTUAL_FUND'] as const;
export const NOTIFICATION_CHANNELS = ['EMAIL', 'SMS', 'PUSH'] as const;
export const NOTIFICATION_TYPES = ['LOAN', 'TRANSACTION', 'SECURITY'] as const;
export const NOTIFICATION_EVENTS = [
  'LOAN_APPLIED', 'LOAN_APPROVED', 'LOAN_REJECTED', 'TRANSFER_COMPLETED', 'LOGIN_DETECTED',
] as const;

