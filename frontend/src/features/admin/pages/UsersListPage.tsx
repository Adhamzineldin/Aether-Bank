import { Users } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { EmptyState } from '@shared/ui/EmptyState';
import { Alert } from '@shared/ui/Alert';

export default function UsersListPage() {
  return (
    <div>
      <PageHeader title="Users" description="Manage customer and employee accounts." />
      <Alert tone="info" className="mb-4">
        A list-users endpoint is planned. Use the audit logs to inspect user activity in the meantime.
      </Alert>
      <Card>
        <CardContent>
          <EmptyState icon={<Users className="h-5 w-5" />} title="No data" description="User search results will be displayed here." />
        </CardContent>
      </Card>
    </div>
  );
}

