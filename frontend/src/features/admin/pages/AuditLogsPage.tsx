import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { http } from '@lib/axios';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { Pagination } from '@shared/ui/Pagination';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { Badge } from '@shared/ui/Badge';
import { Input } from '@shared/ui/Input';
import { FormField } from '@shared/ui/FormField';
import { formatDateTime } from '@shared/utils/date';

interface AuditLog {
  id: string;
  serviceName: string;
  action: string;
  status: 'SUCCESS' | 'FAILED' | string;
  userIdentifier?: string;
  details?: string;
  timestamp: string;
}
interface PageDto<T> { content: T[]; number?: number; totalPages?: number }

export default function AuditLogsPage() {
  const [filters, setFilters] = useState({ serviceName: '', action: '', userId: '' });
  const [page, setPage] = useState(0);

  const logs = useQuery({
    queryKey: ['audit', filters, page],
    queryFn: () =>
      http
        .get<PageDto<AuditLog>>('/api/audit/audit/logs', {
          params: {
            serviceName: filters.serviceName || undefined,
            action: filters.action || undefined,
            userId: filters.userId || undefined,
            page,
            pageSize: 20,
          },
        })
        .then((r) => r.data),
  });

  return (
    <div>
      <PageHeader title="Audit logs" description="Immutable trail of every privileged operation." />
      <Card className="mb-4">
        <CardContent>
          <div className="grid gap-3 md:grid-cols-3">
            <FormField label="Service">
              <Input value={filters.serviceName} onChange={(e) => setFilters((f) => ({ ...f, serviceName: e.target.value }))} placeholder="account-service" />
            </FormField>
            <FormField label="Action">
              <Input value={filters.action} onChange={(e) => setFilters((f) => ({ ...f, action: e.target.value }))} placeholder="TRANSFER_FUNDS" />
            </FormField>
            <FormField label="User ID">
              <Input value={filters.userId} onChange={(e) => setFilters((f) => ({ ...f, userId: e.target.value }))} />
            </FormField>
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardContent>
          {logs.isLoading ? (
            <Skeleton className="h-32" />
          ) : !logs.data?.content?.length ? (
            <p className="text-sm text-muted-fg">No audit events found.</p>
          ) : (
            <>
              <Table>
                <THead>
                  <tr><TH>When</TH><TH>Service</TH><TH>Action</TH><TH>Status</TH><TH>User</TH><TH>Details</TH></tr>
                </THead>
                <TBody>
                  {logs.data.content.map((l) => (
                    <TR key={l.id}>
                      <TD>{formatDateTime(l.timestamp)}</TD>
                      <TD>{l.serviceName}</TD>
                      <TD>{l.action}</TD>
                      <TD>
                        <Badge tone={l.status === 'SUCCESS' ? 'success' : l.status === 'FAILED' ? 'danger' : 'warning'}>{l.status}</Badge>
                      </TD>
                      <TD className="font-mono text-xs">{l.userIdentifier?.slice(0, 8)}…</TD>
                      <TD className="max-w-[280px] truncate">{l.details}</TD>
                    </TR>
                  ))}
                </TBody>
              </Table>
              <Pagination page={logs.data.number || 0} totalPages={logs.data.totalPages || 1} onChange={setPage} />
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
