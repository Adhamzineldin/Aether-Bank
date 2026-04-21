import { useParams, Link } from 'react-router-dom';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Alert } from '@shared/ui/Alert';
import { ROUTES } from '@app/routes';

export default function TransactionDetailPage() {
  const { txId = '' } = useParams();
  return (
    <div>
      <PageHeader title="Transaction" description={<code className="font-mono text-xs">{txId}</code>} back={{ to: ROUTES.transactions }} />
      <Card>
        <CardContent>
          <Alert tone="info">
            Detailed transaction view will load history and trace data once the dedicated endpoint is provided by the backend.
            Meanwhile, please refer to the account statement.
          </Alert>
          <p className="mt-4 text-sm">
            <Link className="text-primary hover:underline" to={ROUTES.transactions}>Back to transactions</Link>
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

