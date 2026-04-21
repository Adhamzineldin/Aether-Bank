import { http } from '@lib/axios';
import type {
  Mortgage,
  MortgageApplication,
  MortgageApplicationResponse,
  MortgageRepaymentSchedule,
} from '@veld/types';

const BASE = '/api/financial_service/mortgages';

export const mortgagesApi = {
  submitApplication: (payload: MortgageApplication) =>
    http
      .post<MortgageApplicationResponse>(`${BASE}/application`, payload)
      .then((r) => r.data),

  get: (id: string) => http.get<Mortgage>(`${BASE}/${id}`).then((r) => r.data),

  listByCustomer: (customerId: string) =>
    http.get<Mortgage[]>(`${BASE}/customer/${customerId}`).then((r) => r.data),

  schedule: (id: string) =>
    http
      .get<MortgageRepaymentSchedule[]>(`${BASE}/${id}/schedule`)
      .then((r) => r.data),
};
