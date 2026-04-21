import { http } from '@lib/axios';
import type { AccountType, AccountStatus } from '@veld/types';

/**
 * Matches the real backend DTO `com.maayn.accountservice.dto.AccountResponse`.
 * Flat shape — NOT the nested `{ account, balance }` shape the generated Veld
 * SDK assumes. Always prefer this type for new code touching account-service.
 */
export interface Account {
  id: string;
  accountNumber: string;
  /** Synthetic IBAN the backend generates from the account number. */
  iban?: string | null;
  customerId: string;
  accountType: AccountType;
  status: AccountStatus;
  currency: string;
  openedDate: string;
  closedDate?: string | null;
  createdAt: string;
  updatedAt: string;
  balance: string | number;
}

export interface OpenAccountInput {
  customerId: string;
  accountType: AccountType;
  currency: string;
  initialDeposit?: string | number;
}

export interface CloseAccountInput {
  reason: string;
  transferToAccountId?: string;
}

export interface UpdateAccountStatusInput {
  status: AccountStatus;
  reason?: string;
}

const BASE = '/api/accounts';

export const accountsApi = {
  open: (input: OpenAccountInput) =>
    http.post<Account>(BASE, input).then((r) => r.data),
  get: (id: string) =>
    http.get<Account>(`${BASE}/${id}`).then((r) => r.data),
  listByCustomer: (customerId: string) =>
    http.get<Account[]>(`${BASE}/customer/${customerId}`).then((r) => r.data),
  close: (id: string, input: CloseAccountInput) =>
    http.post<Account>(`${BASE}/${id}/close`, input).then((r) => r.data),
  updateStatus: (id: string, input: UpdateAccountStatusInput) =>
    http.put<Account>(`${BASE}/${id}/status`, input).then((r) => r.data),
  exists: (id: string) =>
    http.get<boolean>(`${BASE}/${id}/exists`).then((r) => r.data),
  /**
   * Resolve a human-facing account number (or synthetic IBAN tail) to its
   * {@link Account}. Used by the transfer page to convert user-entered
   * account numbers into UUIDs before calling the transfer endpoint.
   */
  getByNumber: (accountNumber: string) =>
    http.get<Account>(`${BASE}/by-number/${encodeURIComponent(accountNumber)}`).then((r) => r.data),
};

