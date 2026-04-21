import { Link } from 'react-router-dom';
import { CreditCard, Plus } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Button } from '@shared/ui/Button';
import { EmptyState } from '@shared/ui/EmptyState';
import { Card, CardContent } from '@shared/ui/Card';
import { Alert } from '@shared/ui/Alert';
import { ROUTES } from '@app/routes';

export default function CardsListPage() {
  // Backend currently exposes get-by-id only. List would need additional endpoint.
  // We surface a polished UI with placeholder + entry to issue a card.
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
      <Alert tone="info" className="mb-4">
        Visit a specific card by ID, or request a new card. A card-list endpoint will be added soon.
      </Alert>
      <Card>
        <CardContent>
          <EmptyState
            icon={<CreditCard className="h-5 w-5" />}
            title="No cards loaded"
            description="Use 'Issue card' to request your first physical or virtual card."
          />
        </CardContent>
      </Card>
    </div>
  );
}

