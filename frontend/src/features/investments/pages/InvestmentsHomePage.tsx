import { Link } from 'react-router-dom';
import { TrendingUp, TrendingDown } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { EmptyState } from '@shared/ui/EmptyState';
import { Badge } from '@shared/ui/Badge';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { ROUTES } from '@app/routes';
import { useAssets } from '../hooks';

export default function InvestmentsHomePage() {
  const { data, isLoading } = useAssets();

  return (
    <div>
      <PageHeader title="Investments" description="Trade stocks, ETFs, bonds and mutual funds." />
      <Card>
        <CardContent>
          {isLoading ? (
            <Skeleton className="h-32" />
          ) : !data?.length ? (
            <EmptyState icon={<TrendingUp className="h-5 w-5" />} title="No assets available" />
          ) : (
            <Table>
              <THead>
                <tr><TH>Symbol</TH><TH>Name</TH><TH>Type</TH><TH className="text-right">Price</TH><TH><span className="sr-only">Actions</span></TH></tr>
              </THead>
              <TBody>
                {data.map((a) => (
                  <TR key={a.symbol}>
                    <TD className="font-mono font-semibold">{a.symbol}</TD>
                    <TD>{a.name}</TD>
                    <TD><Badge>{a.assetType}</Badge></TD>
                    <TD className="text-right"><CurrencyDisplay amount={a.currentPrice} currency={a.currency} /></TD>
                    <TD className="text-right">
                      <Link to={ROUTES.investmentAsset(a.symbol)} className="text-primary text-sm hover:underline">Trade</Link>
                    </TD>
                  </TR>
                ))}
              </TBody>
            </Table>
          )}
        </CardContent>
      </Card>
      <p className="mt-4 text-xs text-muted-fg flex items-center gap-2">
        <TrendingDown className="h-3.5 w-3.5" /> Investments carry risk. Past performance is not indicative of future returns.
      </p>
    </div>
  );
}


