import { useParams, Link } from 'react-router-dom';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Stat } from '@shared/ui/Stat';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { Badge } from '@shared/ui/Badge';
import { ROUTES } from '@app/routes';
import { useAsset } from '../hooks';

export default function AssetDetailPage() {
  const { symbol = '' } = useParams();
  const { data, isLoading } = useAsset(symbol);

  if (isLoading || !data) return <Skeleton className="h-64" />;
  return (
    <div className="space-y-6">
      <PageHeader title={data.symbol} description={data.name} back={{ to: ROUTES.investmentAssets }} />
      <div className="grid gap-4 md:grid-cols-3">
        <Stat label="Price" value={<CurrencyDisplay amount={data.currentPrice} currency={data.currency} />} />
        <Stat label="Type" value={<Badge>{data.assetType}</Badge>} />
        <Stat label="Currency" value={data.currency} />
      </div>
      <Card>
        <CardContent>
          <p className="text-sm text-muted-fg">
            Open an investment account to trade {data.symbol}. Use the link below to view your portfolio and place orders.
          </p>
          <Link to={ROUTES.investments} className="mt-3 inline-block text-primary text-sm hover:underline">Go to investments</Link>
        </CardContent>
      </Card>
    </div>
  );
}

