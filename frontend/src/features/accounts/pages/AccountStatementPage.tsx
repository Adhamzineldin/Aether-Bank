import { useParams } from 'react-router-dom';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { EmptyState } from '@shared/ui/EmptyState';
import { ROUTES } from '@app/routes';
import { useAccount } from '../hooks';

export default function AccountStatementPage() {
  const { accountId = '' } = useParams();
  const { data: a } = useAccount(accountId);

  return (
    <div className="space-y-4">
      <PageHeader
        title="Account statement"
        description={a ? `${a.accountType} · ${a.accountNumber}` : '...'}
        back={{ to: ROUTES.account(accountId) }}
      />
      <Card>
        <CardContent>
          <EmptyState
            title="No transactions yet"
            description="Transactions will appear here once funds move through this account."
          />
        </CardContent>
      </Card>
    </div>
  );
}
