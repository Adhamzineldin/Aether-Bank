import { Link } from 'react-router-dom';
import { Users, ShieldCheck, Bell, ListChecks } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { ROUTES } from '@app/routes';

const tiles = [
  { to: ROUTES.adminUsers, icon: Users, title: 'Users', desc: 'Manage customers and employees.' },
  { to: ROUTES.adminRoles, icon: ShieldCheck, title: 'Roles', desc: 'Roles and permissions.' },
  { to: ROUTES.audit, icon: ShieldCheck, title: 'Audit logs', desc: 'System-wide activity trail.' },
  { to: ROUTES.templates, icon: Bell, title: 'Notification templates', desc: 'Manage outbound templates.' },
  { to: ROUTES.workflow, icon: ListChecks, title: 'Workflows', desc: 'Approval inbox.' },
];

export default function AdminHomePage() {
  return (
    <div>
      <PageHeader title="Administration" description="Operational and system controls." />
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {tiles.map(({ to, icon: Icon, title, desc }) => (
          <Link to={to} key={to}>
            <Card className="p-5 transition-colors hover:border-primary/50">
              <CardContent className="p-0 flex gap-4">
                <span className="grid h-10 w-10 place-items-center rounded-xl bg-primary/10 text-primary">
                  <Icon className="h-5 w-5" />
                </span>
                <div>
                  <p className="font-semibold">{title}</p>
                  <p className="text-sm text-muted-fg">{desc}</p>
                </div>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}

