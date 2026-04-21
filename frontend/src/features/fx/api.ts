import { http } from '@lib/axios';

const BASE = '/api/financial_service/fx';

export interface FxRateResponse {
  sourceCurrency: string;
  destinationCurrency: string;
  exchangeRate: string | number;
  timestamp: string;
}

export const fxApi = {
  /** GET `/api/financial_service/fx/rate?from=USD&to=EUR`. */
  getRate: (from: string, to: string) =>
    http
      .get<FxRateResponse>(`${BASE}/rate`, { params: { from, to } })
      .then((r) => r.data),
};
