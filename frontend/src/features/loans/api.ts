import { http } from '@lib/axios';
import type { LoanApplication, LoanApplicationResponse } from '@veld/types';

const BASE = '/api/financial_service';

export interface LoanProduct {
  id: string;
  code: string;
  name: string;
  loanType: string;
  baseAnnualRate: string | number;
  monthlyFee?: string | number;
  penaltyRate?: string | number;
  defaultTenureMonths: number;
  minimumTenureMonths: number;
  maximumTenureMonths: number;
  minimumPrincipal: string | number;
  maximumPrincipal: string | number;
  interestMethod?: string;
  rateMode?: string;
  repaymentMethod?: string;
  active: boolean;
}

export const loansApi = {
  /** Loan product catalog — powers the product dropdown on Apply page. */
  listProducts: () =>
    http.get<LoanProduct[]>(`${BASE}/products/loans`).then((r) => r.data),

  submitApplication: (payload: LoanApplication) =>
    http
      .post<LoanApplicationResponse>(`${BASE}/api/loan/application/submit`, payload)
      .then((r) => r.data),

  listByCustomer: (customerId: string) =>
    http
      .get<LoanApplication[]>(`${BASE}/api/loan/customer/${customerId}`)
      .then((r) => r.data),

  get: (loanId: string) =>
    http.get<LoanApplication>(`${BASE}/api/loan/${loanId}`).then((r) => r.data),
};
