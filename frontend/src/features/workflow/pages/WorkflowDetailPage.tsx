import { useParams } from 'react-router-dom';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Badge } from '@shared/ui/Badge';
import { Stat } from '@shared/ui/Stat';
import { EmptyState } from '@shared/ui/EmptyState';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { ROUTES } from '@app/routes';
import { formatDateTime } from '@shared/utils/date';
import { useWorkflow } from '../hooks';

export default function WorkflowDetailPage() {
  const { id = '' } = useParams();
  const { data, isLoading, isError, error } = useWorkflow(id);

  if (isLoading) return <Skeleton className="h-64" />;

  if (isError || !data) {
    return (
      <div className="space-y-6">
        <PageHeader title="Workflow" description={`ID ${id}`} back={{ to: ROUTES.workflow }} />
        <Card>
          <CardContent>
            <EmptyState
              title="Workflow not found"
              description={(error as Error)?.message || 'This workflow does not exist or has been removed.'}
            />
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Workflow ${String(data.id).slice(0, 8)}`}
        description={`${data.entityType} · ${String(data.entityId).slice(0, 8)}`}
        back={{ to: ROUTES.workflow }}
      />
      <div className="grid gap-4 md:grid-cols-4">
        <Stat
          label="Status"
          value={<Badge tone={data.status === 'APPROVED' ? 'success' : data.status === 'REJECTED' ? 'danger' : 'warning'}>{data.status}</Badge>}
        />
        <Stat label="Current step" value={String(data.currentStep ?? 0)} />
        <Stat label="Version" value={String(data.version ?? 1)} />
        <Stat label="Updated" value={data.updatedAt ? formatDateTime(data.updatedAt) : '—'} />
      </div>
      <Card>
        <CardContent>
          <h3 className="mb-3 text-sm font-semibold">Steps</h3>
          {(() => {
            const steps = (data as any).steps as any[] | undefined;
            if (!steps?.length) {
              return <p className="text-sm text-muted-fg">No steps defined on this workflow.</p>;
            }
            return (
              <Table>
                <THead>
                  <tr>
                    <TH>#</TH>
                    <TH>Role</TH>
                    <TH>Assignee</TH>
                    <TH>Status</TH>
                    <TH>Decision</TH>
                  </tr>
                </THead>
                <TBody>
                  {steps.map((s: any, i: number) => (
                    <TR key={i}>
                      <TD>{i + 1}</TD>
                      <TD><Badge>{s.role || '—'}</Badge></TD>
                      <TD className="font-mono text-xs">{s.assignedTo ? String(s.assignedTo).slice(0, 8) : '—'}</TD>
                      <TD><Badge tone={s.taskStatus === 'COMPLETED' ? 'success' : 'warning'}>{s.taskStatus || '—'}</Badge></TD>
                      <TD>{s.decisionStatus || '—'}</TD>
                    </TR>
                  ))}
                </TBody>
              </Table>
            );
          })()}
        </CardContent>
      </Card>
    </div>
  );
}
