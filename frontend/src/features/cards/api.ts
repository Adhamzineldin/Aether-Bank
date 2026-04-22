import { http } from '@lib/axios';
import type { CardDetailsResponse, CardSummary } from '@veld/types';

const BASE = '/api/card';

export interface IssueCardRequest {
  customerId: string;
  /** Required for DEBIT — ignored for CREDIT (a fresh credit account is provisioned). */
  accountId?: string;
  cardType: 'DEBIT' | 'CREDIT';
  cardNetwork: 'VISA' | 'MASTERCARD' | 'AMEX';
  currency?: string;
  creditLimit?: string | number;
  annualInterestRate?: string | number;
}

export interface PanRevealResponse {
  pan: string;
  /** 3 digits (Visa/MC) or 4 (AmEx). */
  cvv: string;
}

export const cardsApi = {
  issue: (payload: IssueCardRequest) =>
    http.post<CardSummary>(BASE, payload).then((r) => r.data),

  listByCustomer: (customerId: string) =>
    http.get<CardSummary[]>(`${BASE}/customer/${customerId}`).then((r) => r.data),

  get: (cardId: string) =>
    http.get<CardDetailsResponse>(`${BASE}/${cardId}`).then((r) => r.data),

  /** Full PAN (digits only). Intentionally separate from {@link get} so the number stays hidden until the user reveals it. */
  revealPan: (cardId: string) =>
    http.get<PanRevealResponse>(`${BASE}/${cardId}/pan`).then((r) => r.data),
};
