import { ShieldCheck } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Badge } from '@shared/ui/Badge';
import { Skeleton } from '@shared/ui/Skeleton';
import { useRoles } from '../hooks';

const descriptions: Record<string, string> = {
  SUPERADMIN: 'Bootstrap account with full platform control, including role management.',
  ADMIN: 'Administrative access: manage users, templates and audit logs.',
  EMPLOYEE: 'Internal staff: approve workflows and inspect audit trail.',
  CUSTOMER: 'Self-service banking — accounts, transfers, cards, loans.',
};

const tones: Record<string, 'danger' | 'warning' | 'info' | 'success' | 'neutral'> = {
  SUPERADMIN: 'danger',
  ADMIN: 'warning',
  EMPLOYEE: 'info',
  CUSTOMER: 'success',
};

export default function RolesPage() {
  const roles = useRoles();

  return (
    <div>
      <PageHeader title="Roles & permissions" description="Role-based access control registered with the IAM service." />
      <Card>
        <CardContent className="space-y-3">
          {roles.isLoading ? (
            <Skeleton className="h-32" />
          ) : (roles.data ?? []).length === 0 ? (
            <p className="text-sm text-muted-fg">No roles registered.</p>
          ) : (
            (roles.data ?? []).map((r) => (
              <div key={r} className="flex items-center justify-between rounded-lg border border-border p-3">
                <div className="flex items-center gap-3">
                  <span className="grid h-9 w-9 place-items-center rounded-lg bg-primary/10 text-primary">
                    <ShieldCheck className="h-4 w-4" />
                  </span>
                  <div>
                    <p className="font-semibold">{r}</p>
                    <p className="text-xs text-muted-fg">{descriptions[r] || 'Custom role.'}</p>
                  </div>
                </div>
                <Badge tone={tones[r] ?? 'neutral'}>{r}</Badge>
              </div>
            ))
          )}
        </CardContent>
      </Card>
    </div>
  );
}
