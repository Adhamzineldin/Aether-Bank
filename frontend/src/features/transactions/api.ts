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
};
