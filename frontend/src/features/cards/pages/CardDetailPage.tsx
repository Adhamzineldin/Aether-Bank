import { useParams, Link } from 'react-router-dom';
import { CreditCard, ListOrdered } from 'lucide-react';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Stat } from '@shared/ui/Stat';
import { Button } from '@shared/ui/Button';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { ROUTES } from '@app/routes';
import { CardVisual } from '../components/CardVisual';
import { useCard } from '../hooks';

export default function CardDetailPage() {
  const { cardId = '' } = useParams();
  const { data, isLoading } = useCard(cardId);

  if (isLoading || !data) return <Skeleton className="h-64" />;

  return (
    <div className="space-y-6">
      <PageHeader
        title={`${data.card.cardNetwork} ${data.card.cardType}`}
        description={`•••• ${data.card.lastFourDigits}`}
        back={{ to: ROUTES.cards }}
        actions={
          <Link to={ROUTES.cardTransactions(cardId)}>
            <Button variant="outline" leftIcon={<ListOrdered className="h-4 w-4" />}>Transactions</Button>
          </Link>
        }
      />
      <div className="grid gap-6 md:grid-cols-[auto,1fr] items-start">
        <CardVisual card={data.card} />
        <div className="grid gap-4 sm:grid-cols-2">
          <Stat label="Status" value={data.card.status} icon={<CreditCard className="h-4 w-4" />} />
          {data.creditDetails && (
            <>
              <Stat label="Credit limit" value={<CurrencyDisplay amount={data.creditDetails.creditLimit} />} />
              <Stat label="Available credit" value={<CurrencyDisplay amount={data.creditDetails.availableCredit} />} />
              <Stat label="Current balance" value={<CurrencyDisplay amount={data.creditDetails.currentBalance} />} />
              <Stat label="Min payment" value={<CurrencyDisplay amount={data.creditDetails.minimumPayment} />} />
              <Stat label="APR" value={`${data.creditDetails.annualInterestRate}%`} />
            </>
          )}
        </div>
      </div>
      <Card>
        <CardContent>
          <p className="text-sm text-muted-fg">
            Use the cards UI to freeze, reissue, or set a new PIN. Sensitive operations are guarded by step-up authentication.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

