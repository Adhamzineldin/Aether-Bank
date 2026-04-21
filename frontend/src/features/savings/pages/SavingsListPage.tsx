import { Link } from 'react-router-dom';
import { PiggyBank, Plus } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Button } from '@shared/ui/Button';
import { EmptyState } from '@shared/ui/EmptyState';
import { Card, CardContent } from '@shared/ui/Card';
import { ROUTES } from '@app/routes';

export default function SavingsListPage() {
  return (
    <div>
      <PageHeader
        title="Savings & certificates"
        description="High-yield deposits and term certificates of deposit (CDs)."
        actions={
          <Link to={ROUTES.applySavings}>
            <Button leftIcon={<Plus className="h-4 w-4" />}>Open certificate</Button>
          </Link>
        }
      />
      <Card>
        <CardContent>
          <EmptyState
            icon={<PiggyBank className="h-5 w-5" />}
            title="No certificates yet"
            description="Open a certificate of deposit to lock in higher interest rates."
            action={<Link to={ROUTES.applySavings}><Button>Open certificate</Button></Link>}
          />
        </CardContent>
      </Card>
    </div>
  );
}

