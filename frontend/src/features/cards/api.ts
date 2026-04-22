import { http } from '@lib/axios';
import type {
  CardDetailsResponse,
  CardSummary,
  CardTransactionResponse,
  MerchantPaymentRequest,
  PaginatedCardTransactionResponse,
  RefundCardTransactionRequest,
  VoidCardTransactionRequest,
} from '@veld/types';

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

  processMerchantPayment: (payload: MerchantPaymentRequest) =>
    http.post<CardTransactionResponse>(`${BASE}/payments`, payload).then((r) => r.data),

  refundTransaction: (payload: RefundCardTransactionRequest) =>
    http.post<CardTransactionResponse>(`${BASE}/refunds`, payload).then((r) => r.data),

  voidTransaction: (payload: VoidCardTransactionRequest) =>
    http.post<CardTransactionResponse>(`${BASE}/voids`, payload).then((r) => r.data),

  /**
   * Card transaction history. The veld spec models this as GET-with-body
   * which browsers can't reliably send, so card-service exposes a
   * query-string variant on the same path that we hit here.
   */
  listTransactions: (
    cardId: string,
    params: { page?: number; pageSize?: number; status?: string; type?: string } = {},
  ) =>
    http
      .get<PaginatedCardTransactionResponse>(`${BASE}/${cardId}/transactions`, { params })
      .then((r) => r.data),
};
