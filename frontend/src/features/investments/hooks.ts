import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useVeld } from '@shared/hooks/useVeld';
import type { BuyAssetRequest, OpenInvestmentAccountRequest, SellAssetRequest } from '@veld/types';

export const investmentKeys = {
  account: (id: string) => ['investments', 'account', id] as const,
  portfolio: (id: string) => ['investments', 'portfolio', id] as const,
  performance: (id: string) => ['investments', 'performance', id] as const,
  assets: () => ['investments', 'assets'] as const,
  asset: (s: string) => ['investments', 'assets', s] as const,
};

export function useInvestmentAccount(id: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: investmentKeys.account(id || ''),
    enabled: !!id,
    queryFn: () => veld.investment.getInvestmentAccount(id as string),
  });
}

export function usePortfolio(id: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: investmentKeys.portfolio(id || ''),
    enabled: !!id,
    queryFn: () => veld.investment.getPortfolio(id as string, {} as any),
  });
}

export function usePerformance(id: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: investmentKeys.performance(id || ''),
    enabled: !!id,
    queryFn: () => veld.investment.getPerformance(id as string, {} as any),
  });
}

export function useAssets() {
  const veld = useVeld();
  return useQuery({ queryKey: investmentKeys.assets(), queryFn: () => veld.investment.listAssets() });
}

export function useAsset(symbol: string | undefined) {
  const veld = useVeld();
  return useQuery({
    queryKey: investmentKeys.asset(symbol || ''),
    enabled: !!symbol,
    queryFn: () => veld.investment.getAsset(symbol as string),
  });
}

export function useOpenInvestmentAccount() {
  const veld = useVeld();
  return useMutation({
    mutationFn: (input: OpenInvestmentAccountRequest) => veld.investment.openInvestmentAccount(input),
    onSuccess: () => toast.success('Investment account opened'),
  });
}

export function useBuyAsset(accountId: string) {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: BuyAssetRequest) => veld.investment.buyAsset(accountId, input),
    onSuccess: () => {
      toast.success('Buy order placed');
      qc.invalidateQueries({ queryKey: investmentKeys.portfolio(accountId) });
    },
  });
}

export function useSellAsset(accountId: string) {
  const veld = useVeld();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: SellAssetRequest) => veld.investment.sellAsset(accountId, input),
    onSuccess: () => {
      toast.success('Sell order placed');
      qc.invalidateQueries({ queryKey: investmentKeys.portfolio(accountId) });
    },
  });
}

