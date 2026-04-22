import { Fragment, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { http } from '@lib/axios';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { Pagination } from '@shared/ui/Pagination';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { Badge } from '@shared/ui/Badge';
import { Input } from '@shared/ui/Input';
import { FormField } from '@shared/ui/FormField';
import { CopyButton } from '@shared/ui/CopyButton';
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

/** Best-effort pretty print: parses JSON when possible, falls back to raw text. */
function prettyDetails(raw?: string): string {
  if (!raw) return '—';
  const trimmed = raw.trim();
  if ((trimmed.startsWith('{') && trimmed.endsWith('}')) || (trimmed.startsWith('[') && trimmed.endsWith(']'))) {
    try {
      return JSON.stringify(JSON.parse(trimmed), null, 2);
    } catch {
      // not valid JSON — fall through
    }
  }
  return raw;
}

function shortId(id?: string): string {
  if (!id) return '—';
  if (id.length <= 12) return id;
  return `${id.slice(0, 8)}…${id.slice(-4)}`;
}

export default function AuditLogsPage() {
  const [filters, setFilters] = useState({ serviceName: '', action: '', userId: '' });
  const [page, setPage] = useState(0);
  const [expanded, setExpanded] = useState<Set<string>>(new Set());

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

  const toggle = (id: string) =>
    setExpanded((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });

  const rows = useMemo(() => logs.data?.content ?? [], [logs.data]);

  return (
    <div>
      <PageHeader title="Audit logs" description="Immutable trail of every privileged operation." />
      <Card className="mb-4">
        <CardContent>
          <div className="grid gap-3 md:grid-cols-3">
            <FormField label="Service">
              <Input
                value={filters.serviceName}
                onChange={(e) => { setPage(0); setFilters((f) => ({ ...f, serviceName: e.target.value })); }}
                placeholder="account-service"
              />
            </FormField>
            <FormField label="Action">
              <Input
                value={filters.action}
                onChange={(e) => { setPage(0); setFilters((f) => ({ ...f, action: e.target.value })); }}
                placeholder="TRANSFER_FUNDS"
              />
            </FormField>
            <FormField label="User ID">
              <Input
                value={filters.userId}
                onChange={(e) => { setPage(0); setFilters((f) => ({ ...f, userId: e.target.value })); }}
              />
            </FormField>
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardContent>
          {logs.isLoading ? (
            <Skeleton className="h-32" />
          ) : !rows.length ? (
            <p className="text-sm text-muted-fg">No audit events found.</p>
          ) : (
            <>
              <Table className="table-fixed">
                <THead>
                  <tr>
                    <TH className="w-10"> </TH>
                    <TH className="w-48">When</TH>
                    <TH className="w-44">Service</TH>
                    <TH className="w-56">Action</TH>
                    <TH className="w-28">Status</TH>
                    <TH className="w-40">User</TH>
                    <TH>Details</TH>
                  </tr>
                </THead>
                <TBody>
                  {rows.map((l) => {
                    const isOpen = expanded.has(l.id);
                    return (
                      <Fragment key={l.id}>
                        <TR
                          className="cursor-pointer"
                          onClick={() => toggle(l.id)}
                        >
                          <TD>
                            {isOpen
                              ? <ChevronDown className="h-4 w-4 text-muted-fg" />
                              : <ChevronRight className="h-4 w-4 text-muted-fg" />}
                          </TD>
                          <TD className="whitespace-nowrap">{formatDateTime(l.timestamp)}</TD>
                          <TD className="truncate" title={l.serviceName}>{l.serviceName}</TD>
                          <TD className="truncate" title={l.action}>
                            <span className="font-mono text-xs">{l.action}</span>
                          </TD>
                          <TD>
                            <Badge tone={l.status === 'SUCCESS' ? 'success' : l.status === 'FAILED' ? 'danger' : 'warning'}>
                              {l.status}
                            </Badge>
                          </TD>
                          <TD className="font-mono text-xs truncate" title={l.userIdentifier}>
                            {shortId(l.userIdentifier)}
                          </TD>
                          <TD className="truncate text-muted-fg" title={l.details}>
                            {l.details ? l.details.replace(/\s+/g, ' ').slice(0, 120) : '—'}
                          </TD>
                        </TR>
                        {isOpen && (
                          <tr key={`${l.id}-details`} className="bg-muted/20">
                            <td />
                            <td colSpan={6} className="px-4 py-4">
                              <div className="grid gap-4 md:grid-cols-2">
                                <div>
                                  <div className="text-xs uppercase tracking-wide text-muted-fg mb-1">Event ID</div>
                                  <div className="flex items-center gap-2">
                                    <code className="font-mono text-xs break-all">{l.id}</code>
                                    <CopyButton value={l.id} className="h-7 px-2" />
                                  </div>
                                </div>
                                <div>
                                  <div className="text-xs uppercase tracking-wide text-muted-fg mb-1">User</div>
                                  <div className="flex items-center gap-2">
                                    <code className="font-mono text-xs break-all">{l.userIdentifier ?? '—'}</code>
                                    {l.userIdentifier && <CopyButton value={l.userIdentifier} className="h-7 px-2" />}
                                  </div>
                                </div>
                              </div>
                              <div className="mt-4">
                                <div className="text-xs uppercase tracking-wide text-muted-fg mb-1 flex items-center gap-2">
                                  <span>Details</span>
                                  {l.details && <CopyButton value={l.details} className="h-6 px-2 text-[10px]" />}
                                </div>
                                <pre className="whitespace-pre-wrap break-words rounded-md border border-border bg-background p-3 text-xs font-mono leading-relaxed max-h-[480px] overflow-auto">
{prettyDetails(l.details)}
                                </pre>
                              </div>
                            </td>
                          </tr>
                        )}
                      </Fragment>
                    );
                  })}
                </TBody>
              </Table>
              <Pagination page={logs.data?.number || 0} totalPages={logs.data?.totalPages || 1} onChange={setPage} />
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
