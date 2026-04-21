import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Users, Search } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { EmptyState } from '@shared/ui/EmptyState';
import { Skeleton } from '@shared/ui/Skeleton';
import { Input } from '@shared/ui/Input';
import { Badge } from '@shared/ui/Badge';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { ROUTES } from '@app/routes';
import { useAdminUsers } from '../hooks';

export default function UsersListPage() {
  const users = useAdminUsers();
  const [q, setQ] = useState('');

  const filtered = useMemo(() => {
    const data = users.data ?? [];
    if (!q.trim()) return data;
    const n = q.trim().toLowerCase();
    return data.filter(
      (u) =>
        u.username.toLowerCase().includes(n) ||
        u.email?.toLowerCase().includes(n) ||
        u.roles.some((r) => r.toLowerCase().includes(n)),
    );
  }, [users.data, q]);

  return (
    <div>
      <PageHeader title="Users" description="Manage customer, employee, admin and superadmin accounts." />
      <Card className="mb-4">
        <CardContent>
          <div className="flex items-center gap-2">
            <Search className="h-4 w-4 text-muted-fg" />
            <Input placeholder="Search by username, email or role…" value={q} onChange={(e) => setQ(e.target.value)} />
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardContent>
          {users.isLoading ? (
            <Skeleton className="h-32" />
          ) : filtered.length === 0 ? (
            <EmptyState
              icon={<Users className="h-5 w-5" />}
              title="No users found"
              description={q ? 'Nothing matches your filter.' : 'No users have registered yet.'}
            />
          ) : (
            <Table>
              <THead>
                <tr>
                  <TH>Username</TH>
                  <TH>Email</TH>
                  <TH>Roles</TH>
                  <TH>Status</TH>
                  <TH className="text-right">Actions</TH>
                </tr>
              </THead>
              <TBody>
                {filtered.map((u) => (
                  <TR key={u.id}>
                    <TD className="font-medium">{u.username}</TD>
                    <TD>{u.email}</TD>
                    <TD>
                      <div className="flex flex-wrap gap-1">
                        {u.roles.map((r) => (
                          <Badge key={r} tone={r === 'SUPERADMIN' ? 'danger' : r === 'ADMIN' ? 'warning' : r === 'EMPLOYEE' ? 'info' : 'success'}>{r}</Badge>
                        ))}
                      </div>
                    </TD>
                    <TD>
                      <div className="flex gap-1">
                        <Badge tone={u.isActive ? 'success' : 'neutral'}>{u.isActive ? 'Active' : 'Inactive'}</Badge>
                        {u.isLocked && <Badge tone="danger">Locked</Badge>}
                      </div>
                    </TD>
                    <TD className="text-right">
                      <Link to={ROUTES.adminUser(u.id)} className="text-primary hover:underline text-sm">
                        Manage →
                      </Link>
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
