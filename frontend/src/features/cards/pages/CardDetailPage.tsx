import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { CreditCard, Eye, EyeOff, ListOrdered } from 'lucide-react';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Stat } from '@shared/ui/Stat';
import { Button } from '@shared/ui/Button';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { CopyButton } from '@shared/ui/CopyButton';
import { ROUTES } from '@app/routes';
import { CardVisual } from '../components/CardVisual';
import { useCard } from '../hooks';
import { cardsApi } from '../api';
import { formatPanGroups } from '@shared/utils/mask';

export default function CardDetailPage() {
  const { cardId = '' } = useParams();
  const { data, isLoading } = useCard(cardId);
  const [showPan, setShowPan] = useState(false);

  const panQuery = useQuery({
    queryKey: ['card-pan', cardId],
    queryFn: () => cardsApi.revealPan(cardId),
    enabled: showPan && !!cardId,
  });

  if (isLoading || !data) return <Skeleton className="h-64" />;

  const panDigits = panQuery.data?.pan;
  const cvvDigits = panQuery.data?.cvv;
  const panLoading = showPan && panQuery.isFetching;
  const panError = showPan && panQuery.isError;

  return (
    <div className="space-y-6">
      <PageHeader
        title={`${data.card.cardNetwork} ${data.card.cardType}`}
        description={`•••• ${data.card.lastFourDigits}`}
        back={{ to: ROUTES.cards }}
        actions={
          <div className="flex flex-wrap items-center gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              leftIcon={showPan ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              onClick={() => setShowPan((v) => !v)}
            >
              {showPan ? 'Hide number' : 'Show number'}
            </Button>
            {showPan && panDigits && (
              <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-muted-fg">
                <span className="inline-flex items-center gap-1">
                  PAN
                  <CopyButton value={panDigits.replace(/\D/g, '')} className="h-8 px-2" />
                </span>
                {cvvDigits ? (
                  <span className="inline-flex items-center gap-1">
                    CVV
                    <CopyButton value={cvvDigits} className="h-8 px-2" />
                  </span>
                ) : null}
              </div>
            )}
            <Link to={ROUTES.cardTransactions(cardId)}>
              <Button variant="outline" leftIcon={<ListOrdered className="h-4 w-4" />}>Transactions</Button>
            </Link>
          </div>
        }
      />
      <div className="grid gap-6 md:grid-cols-[auto,1fr] items-start">
        <CardVisual
          card={data.card}
          revealedPanDigits={showPan && panDigits ? panDigits : null}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <Stat label="Status" value={data.card.status} icon={<CreditCard className="h-4 w-4" />} />
          {showPan && (
            <>
              <Stat
                label="Card number"
                value={
                  panLoading ? (
                    <span className="text-muted-fg text-sm">Loading…</span>
                  ) : panError ? (
                    <span className="text-danger text-sm">Could not load number</span>
                  ) : panDigits ? (
                    <span className="font-mono text-sm">{formatPanGroups(panDigits)}</span>
                  ) : (
                    <span className="text-muted-fg text-sm">—</span>
                  )
                }
              />
              <Stat
                label={data.card.cardNetwork === 'AMEX' ? 'CID (security code)' : 'CVV'}
                value={
                  panLoading ? (
                    <span className="text-muted-fg text-sm">Loading…</span>
                  ) : panError ? (
                    <span className="text-danger text-sm">—</span>
                  ) : cvvDigits ? (
                    <span className="font-mono text-sm tracking-widest">{cvvDigits}</span>
                  ) : (
                    <span className="text-muted-fg text-sm">—</span>
                  )
                }
              />
            </>
          )}
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
            The card number and CVV are hidden by default. Use Show number to load them (same request), then copy PAN and/or CVV for test checkouts.
            Demo values are for development only — real issuers never store or show CVV like this.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
