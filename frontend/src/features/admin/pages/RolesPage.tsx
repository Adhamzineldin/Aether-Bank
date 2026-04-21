import { ShieldCheck } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Badge } from '@shared/ui/Badge';
import { ROLES } from '@shared/constants/enums';

export default function RolesPage() {
  return (
    <div>
      <PageHeader title="Roles & permissions" description="Role-based access control summary." />
      <Card>
        <CardContent className="space-y-3">
          {ROLES.map((r) => (
            <div key={r} className="flex items-center justify-between rounded-lg border border-border p-3">
              <div className="flex items-center gap-3">
                <span className="grid h-9 w-9 place-items-center rounded-lg bg-primary/10 text-primary">
                  <ShieldCheck className="h-4 w-4" />
                </span>
                <div>
                  <p className="font-semibold">{r}</p>
                  <p className="text-xs text-muted-fg">{
                    r === 'ADMIN' ? 'Full system access including user/role management.' :
                    r === 'EMPLOYEE' ? 'Approve workflows, inspect audit logs.' :
                    'Self-service banking and personal financial management.'
                  }</p>
                </div>
              </div>
              <Badge tone={r === 'ADMIN' ? 'danger' : r === 'EMPLOYEE' ? 'info' : 'success'}>{r}</Badge>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}

