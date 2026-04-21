import { useParams } from 'react-router-dom';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Alert } from '@shared/ui/Alert';
import { ROUTES } from '@app/routes';

export default function LoanDetailPage() {
  const { loanId = '' } = useParams();
  return (
    <div>
      <PageHeader title="Loan" description={<code className="font-mono text-xs">{loanId}</code>} back={{ to: ROUTES.loans }} />
      <Card>
        <CardContent>
          <Alert tone="info">Detailed loan timeline and repayments will appear once a per-loan endpoint is exposed.</Alert>
        </CardContent>
      </Card>
    </div>
  );
}

