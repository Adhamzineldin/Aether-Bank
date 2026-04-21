import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useStubQuery, unavailableMutation } from '@lib/stub';
import type { BuyAssetRequest, OpenInvestmentAccountRequest, SellAssetRequest } from '@veld/types';

export const investmentKeys = {
  account: (id: string) => ['investments', 'account', id] as const,
  portfolio: (id: string) => ['investments', 'portfolio', id] as const,
  performance: (id: string) => ['investments', 'performance', id] as const,
  assets: () => ['investments', 'assets'] as const,
  asset: (s: string) => ['investments', 'assets', s] as const,
};

export function useInvestmentAccount(id: string | undefined) {
  return useStubQuery(investmentKeys.account(id || ''));
}

export function usePortfolio(id: string | undefined) {
  return useStubQuery<{ holdings: any[]; totalValue?: number; totalCost?: number; unrealizedGainLoss?: number; currency?: string }>(
    investmentKeys.portfolio(id || ''),
    { holdings: [] },
  );
}

export function usePerformance(id: string | undefined) {
  return useStubQuery(investmentKeys.performance(id || ''));
}

export function useAssets() {
  return useStubQuery<any[]>(investmentKeys.assets(), []);
}

export function useAsset(symbol: string | undefined) {
  return useStubQuery(investmentKeys.asset(symbol || ''));
}

export function useOpenInvestmentAccount() {
  return useMutation({
    mutationFn: unavailableMutation<OpenInvestmentAccountRequest, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useBuyAsset(_accountId: string) {
  return useMutation({
    mutationFn: unavailableMutation<BuyAssetRequest, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}

export function useSellAsset(_accountId: string) {
  return useMutation({
    mutationFn: unavailableMutation<SellAssetRequest, void>(),
    onError: (e: Error) => toast.error(e.message),
  });
}
