import { Link } from 'react-router-dom';
import { ListChecks } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { EmptyState } from '@shared/ui/EmptyState';
import { Badge } from '@shared/ui/Badge';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { ROUTES } from '@app/routes';
import { fromNow } from '@shared/utils/date';
import { useMyTasks } from '../hooks';

export default function WorkflowInboxPage() {
  const { data, isLoading } = useMyTasks();
  return (
    <div>
      <PageHeader title="My approval tasks" description="Workflow tasks assigned to you, ranked by urgency." />
      <Card>
        <CardContent>
          {isLoading ? (
            <Skeleton className="h-32" />
          ) : !data?.length ? (
            <EmptyState icon={<ListChecks className="h-5 w-5" />} title="Inbox zero" description="No tasks assigned to you right now." />
          ) : (
            <Table>
              <THead>
                <tr><TH>Workflow</TH><TH>Step</TH><TH>Role</TH><TH>Status</TH><TH>Created</TH><TH><span className="sr-only">Actions</span></TH></tr>
              </THead>
              <TBody>
                {data.map((t) => (
                  <TR key={t.id}>
                    <TD className="font-mono text-xs">{t.workflowId.slice(0, 8)}…</TD>
                    <TD>{t.step}</TD>
                    <TD><Badge>{t.role}</Badge></TD>
                    <TD><Badge tone={t.taskStatus === 'COMPLETED' ? 'success' : 'warning'}>{t.taskStatus}</Badge></TD>
                    <TD>{fromNow(t.createdAt)}</TD>
                    <TD className="text-right">
                      <Link to={ROUTES.workflowDetail(t.workflowId)} className="text-primary text-sm hover:underline">Open</Link>
                    </TD>
                  </TR>
                ))}
              </TBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}


