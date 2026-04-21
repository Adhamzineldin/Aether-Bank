import { useParams } from 'react-router-dom';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Stat } from '@shared/ui/Stat';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { EmptyState } from '@shared/ui/EmptyState';
import { ROUTES } from '@app/routes';
import { usePortfolio, usePerformance, useInvestmentAccount } from '../hooks';

export default function PortfolioPage() {
  const { accountId = '' } = useParams();
  const account = useInvestmentAccount(accountId);
  const portfolio = usePortfolio(accountId);
  const perf = usePerformance(accountId);

  if (account.isLoading || portfolio.isLoading) return <Skeleton className="h-64" />;
  if (!portfolio.data) return <EmptyState title="No portfolio data" />;

  return (
    <div className="space-y-6">
      <PageHeader title="Portfolio" description={account.data?.account.accountNumber} back={{ to: ROUTES.investments }} />
      <div className="grid gap-4 md:grid-cols-4">
        <Stat label="Total value" value={<CurrencyDisplay amount={portfolio.data.totalValue} currency={portfolio.data.currency} />} />
        <Stat label="Total cost" value={<CurrencyDisplay amount={portfolio.data.totalCost} currency={portfolio.data.currency} />} />
        <Stat
          label="Unrealized P/L"
          value={<CurrencyDisplay amount={portfolio.data.unrealizedGainLoss} currency={portfolio.data.currency} signed />}
        />
        <Stat label="Day change" value={perf.data ? `${perf.data.dayChangePercentage}%` : '—'} />
      </div>
      <Card>
        <CardContent>
          <h3 className="mb-3 text-sm font-semibold">Holdings</h3>
          {!portfolio.data.holdings?.length ? (
            <EmptyState title="No holdings yet" description="Buy your first asset to populate your portfolio." />
          ) : (
            <Table>
              <THead>
                <tr><TH>Symbol</TH><TH>Type</TH><TH>Qty</TH><TH>Avg cost</TH><TH>Price</TH><TH className="text-right">Value</TH></tr>
              </THead>
              <TBody>
                {portfolio.data.holdings.map((h) => (
                  <TR key={h.symbol}>
                    <TD className="font-mono font-semibold">{h.symbol}</TD>
                    <TD>{h.assetType}</TD>
                    <TD>{h.quantity}</TD>
                    <TD><CurrencyDisplay amount={h.averagePurchasePrice} /></TD>
                    <TD><CurrencyDisplay amount={h.currentPrice} /></TD>
                    <TD className="text-right font-medium"><CurrencyDisplay amount={h.totalValue} /></TD>
                  </TR>
                ))}
              </TBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

