import { http } from '@lib/axios';
import type { TransferRequest } from '@veld/types';

const BASE = '/api/transaction_service';

export interface TransferResponseDto {
  id: string;
  referenceNumber?: string;
  sourceAccountId: string;
  destinationAccountId: string;
  amount: string;
  currency: string;
  status: string;
  type?: string;
  createdAt?: string;
  executedAt?: string;
}

export interface BalanceResponseDto {
  accountId: string;
  currency: string;
  availableBalance: string;
  pendingBalance?: string;
}

export interface TransactionRowDto {
  referenceNumber: string;
  timestamp: string;
  status: string;
  type: string;
  direction: 'CREDIT' | 'DEBIT';
  /** Amount in the viewing account's own currency (positive magnitude). */
  amount: string;
  /** Viewing account's currency. */
  currency: string;
  counterpartyAccountId: string | null;
  /** Counterparty leg amount — differs from {@link amount} for FX transfers. */
  counterpartyAmount: string | null;
  counterpartyCurrency: string | null;
  exchangeRate: string | null;
}

export interface PaginatedTransactionsDto {
  content: TransactionRowDto[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}

export const transactionsApi = {
  /** POST `/api/transaction_service/transactions/transfer` — full ledger + FX + events. */
  transfer: (payload: TransferRequest) =>
    http
      .post<TransferResponseDto>(`${BASE}/transactions/transfer`, payload)
      .then((r) => r.data),

  /** GET `/api/transaction_service/ledger/{accountId}/{currency}/balance`. */
  balance: (accountId: string, currency: string) =>
    http
      .get<BalanceResponseDto>(`${BASE}/ledger/${accountId}/${currency}/balance`)
      .then((r) => r.data),

  /**
   * GET `/api/transaction_service/transactions/history/{accountId}` — paginated
   * account history. Uses the hand-written query-param endpoint because the
   * Veld-generated action requires a GET body which browsers can't send.
   */
  history: (accountId: string, currency: string, page = 0, pageSize = 20) =>
    http
      .get<PaginatedTransactionsDto>(`${BASE}/transactions/history/${accountId}`, {
        params: { currency, page, pageSize },
      })
      .then((r) => r.data),
};
