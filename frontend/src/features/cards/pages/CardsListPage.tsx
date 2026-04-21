import { Link } from 'react-router-dom';
import { CreditCard, Plus } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Button } from '@shared/ui/Button';
import { EmptyState } from '@shared/ui/EmptyState';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { Badge } from '@shared/ui/Badge';
import { ROUTES } from '@app/routes';
import { useMyCards } from '../hooks';

export default function CardsListPage() {
  const { data, isLoading } = useMyCards();
  return (
    <div>
      <PageHeader
        title="Cards"
        description="Your debit and credit cards."
        actions={
          <Link to={ROUTES.issueCard}>
            <Button leftIcon={<Plus className="h-4 w-4" />}>Issue card</Button>
          </Link>
        }
      />
      {isLoading ? (
        <Skeleton className="h-40" />
      ) : !data?.length ? (
        <Card>
          <CardContent>
            <EmptyState
              icon={<CreditCard className="h-5 w-5" />}
              title="No cards yet"
              description="Use 'Issue card' to request your first debit or credit card."
            />
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((c) => (
            <Link key={c.id} to={ROUTES.card(c.id)}>
              <Card className="hover:shadow-md transition-shadow cursor-pointer">
                <CardContent className="space-y-3">
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="text-xs uppercase tracking-wide text-muted-fg">
                        {c.cardNetwork} · {c.cardType}
                      </div>
                      <div className="font-mono text-lg">•••• {c.lastFourDigits}</div>
                    </div>
                    <Badge tone={c.status === 'ACTIVE' ? 'success' : c.status === 'BLOCKED' ? 'danger' : 'warning'}>
                      {c.status}
                    </Badge>
                  </div>
                  <div className="text-xs text-muted-fg">
                    Exp {String(c.expiryMonth).padStart(2, '0')}/{String(c.expiryYear).slice(-2)}
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
