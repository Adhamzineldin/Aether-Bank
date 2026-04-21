import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Lock, Unlock, CheckCircle2, XCircle, Trash2, Plus } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { Badge } from '@shared/ui/Badge';
import { Alert } from '@shared/ui/Alert';
import { Button } from '@shared/ui/Button';
import { Select } from '@shared/ui/Select';
import { FormField } from '@shared/ui/FormField';
import { ROUTES } from '@app/routes';
import { useAuthStore } from '@stores/authStore';
import {
  useAdminUser,
  useRoles,
  useAssignRole,
  useRemoveRole,
  useLockUser,
  useUnlockUser,
  useActivateUser,
  useDeactivateUser,
} from '../hooks';
import { formatDateTime } from '@shared/utils/date';

export default function UserDetailPage() {
  const { id = '' } = useParams();
  const userQ = useAdminUser(id);
  const rolesQ = useRoles();
  const canMutate = useAuthStore((s) => s.hasRole)('SUPERADMIN');

  const assignRole = useAssignRole();
  const removeRole = useRemoveRole();
  const lock = useLockUser();
  const unlock = useUnlockUser();
  const activate = useActivateUser();
  const deactivate = useDeactivateUser();

  const [newRole, setNewRole] = useState('');

  if (userQ.isLoading) return <Skeleton className="h-64" />;
  if (userQ.isError || !userQ.data) {
    return (
      <div className="space-y-4">
        <PageHeader title="User" back={{ to: ROUTES.adminUsers }} />
        <Alert tone="warning">Unable to load this user. Check the IAM service status.</Alert>
      </div>
    );
  }

  const u = userQ.data;
  const availableRoles = (rolesQ.data ?? []).filter((r) => !u.roles.includes(r));

  return (
    <div className="space-y-6">
      <PageHeader
        title={u.username}
        description={u.email}
        back={{ to: ROUTES.adminUsers }}
        actions={
          canMutate ? (
            <>
              {u.isLocked ? (
                <Button variant="outline" loading={unlock.isPending} onClick={() => unlock.mutate({ id: u.id })} leftIcon={<Unlock className="h-4 w-4" />}>
                  Unlock
                </Button>
              ) : (
                <Button variant="outline" loading={lock.isPending} onClick={() => lock.mutate({ id: u.id })} leftIcon={<Lock className="h-4 w-4" />}>
                  Lock
                </Button>
              )}
              {u.isActive ? (
                <Button variant="outline" loading={deactivate.isPending} onClick={() => deactivate.mutate({ id: u.id })} leftIcon={<XCircle className="h-4 w-4" />}>
                  Deactivate
                </Button>
              ) : (
                <Button variant="outline" loading={activate.isPending} onClick={() => activate.mutate({ id: u.id })} leftIcon={<CheckCircle2 className="h-4 w-4" />}>
                  Activate
                </Button>
              )}
            </>
          ) : null
        }
      />

      {!canMutate && (
        <Alert tone="info">Only SUPERADMIN accounts can change roles or account status.</Alert>
      )}

      <Card>
        <CardContent className="grid gap-3 md:grid-cols-2 text-sm">
          <Detail label="User ID"><code className="font-mono text-xs">{u.id}</code></Detail>
          <Detail label="Username">{u.username}</Detail>
          <Detail label="Email">{u.email}</Detail>
          <Detail label="Full name">{u.fullName || '—'}</Detail>
          <Detail label="Status">
            <div className="flex gap-1">
              <Badge tone={u.isActive ? 'success' : 'neutral'}>{u.isActive ? 'Active' : 'Inactive'}</Badge>
              {u.isLocked && <Badge tone="danger">Locked</Badge>}
              {u.isEmailVerified && <Badge tone="info">Email verified</Badge>}
              {u.mfaEnabled && <Badge tone="info">MFA</Badge>}
            </div>
          </Detail>
          <Detail label="Created">{u.createdAt ? formatDateTime(u.createdAt) : '—'}</Detail>
          <Detail label="Last login">{u.lastLogin ? formatDateTime(u.lastLogin) : '—'}</Detail>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="space-y-4">
          <h3 className="text-sm font-semibold">Roles</h3>
          <div className="flex flex-wrap gap-2">
            {u.roles.length === 0 && <span className="text-sm text-muted-fg">No roles assigned.</span>}
            {u.roles.map((r) => (
              <span key={r} className="inline-flex items-center gap-1 rounded-full border border-border bg-muted/30 pl-3 pr-1 py-1 text-xs font-medium">
                {r}
                {canMutate && (
                  <button
                    className="ml-1 grid h-5 w-5 place-items-center rounded-full text-muted-fg hover:bg-danger/10 hover:text-danger"
                    disabled={removeRole.isPending}
                    onClick={() => removeRole.mutate({ id: u.id, arg: r })}
                    title={`Remove ${r}`}
                  >
                    <Trash2 className="h-3 w-3" />
                  </button>
                )}
              </span>
            ))}
          </div>

          {canMutate && (
            <div className="flex items-end gap-2 pt-2">
              <FormField label="Assign role" className="flex-1 max-w-xs">
                <Select value={newRole} onChange={(e) => setNewRole(e.target.value)}>
                  <option value="">Choose a role…</option>
                  {availableRoles.map((r) => <option key={r} value={r}>{r}</option>)}
                </Select>
              </FormField>
              <Button
                disabled={!newRole}
                loading={assignRole.isPending}
                leftIcon={<Plus className="h-4 w-4" />}
                onClick={() => {
                  if (!newRole) return;
                  assignRole.mutate({ id: u.id, arg: newRole }, { onSuccess: () => setNewRole('') });
                }}
              >
                Add
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function Detail({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wider text-muted-fg">{label}</p>
      <p className="mt-1">{children}</p>
    </div>
  );
}
