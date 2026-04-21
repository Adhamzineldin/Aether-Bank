import { useParams } from 'react-router-dom';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Alert } from '@shared/ui/Alert';
import { ROUTES } from '@app/routes';

export default function PortfolioPage() {
  const { accountId = '' } = useParams();
  return (
    <div className="space-y-6">
      <PageHeader title="Portfolio" description={`Account ${accountId}`} back={{ to: ROUTES.investments }} />
      <Card>
        <CardContent>
          <Alert tone="info">Investment service is not yet deployed. Portfolio and performance data will appear once the backend is live.</Alert>
        </CardContent>
      </Card>
    </div>
  );
}
