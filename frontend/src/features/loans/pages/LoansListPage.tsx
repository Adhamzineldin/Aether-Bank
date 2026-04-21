import { Link } from 'react-router-dom';
import { ScrollText, Plus } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Button } from '@shared/ui/Button';
import { EmptyState } from '@shared/ui/EmptyState';
import { Card, CardContent } from '@shared/ui/Card';
import { ROUTES } from '@app/routes';

export default function LoansListPage() {
  return (
    <div>
      <PageHeader
        title="Loans"
        description="View and manage your loan applications and active loans."
        actions={
          <Link to={ROUTES.applyLoan}>
            <Button leftIcon={<Plus className="h-4 w-4" />}>Apply for loan</Button>
          </Link>
        }
      />
      <Card>
        <CardContent>
          <EmptyState
            icon={<ScrollText className="h-5 w-5" />}
            title="No loans yet"
            description="Apply for a personal, auto, mortgage, or business loan to get started."
            action={<Link to={ROUTES.applyLoan}><Button>Apply</Button></Link>}
          />
        </CardContent>
      </Card>
    </div>
  );
}

