import { useParams } from 'react-router-dom';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Alert } from '@shared/ui/Alert';
import { ROUTES } from '@app/routes';

export default function WorkflowDetailPage() {
  const { id = '' } = useParams();
  return (
    <div className="space-y-6">
      <PageHeader title="Workflow" description={`ID ${id}`} back={{ to: ROUTES.workflow }} />
      <Card>
        <CardContent>
          <Alert tone="info">Workflow approval service is not yet deployed.</Alert>
        </CardContent>
      </Card>
    </div>
  );
}
