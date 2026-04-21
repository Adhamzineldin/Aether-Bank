import { Link } from 'react-router-dom';
import { Bell, RefreshCw } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { EmptyState } from '@shared/ui/EmptyState';
import { Badge, type BadgeTone } from '@shared/ui/Badge';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';
import { fromNow } from '@shared/utils/date';
import { useNotifications, useRetryNotification } from '../hooks';
import type { NotificationStatus } from '@veld/types';

const tone: Record<NotificationStatus, BadgeTone> = { PENDING: 'warning', SENT: 'success', FAILED: 'danger' };

export default function NotificationsListPage() {
  const { data, isLoading, refetch } = useNotifications();
  const retry = useRetryNotification();

  return (
    <div>
      <PageHeader
        title="Notifications"
        description="Email, SMS, and push notifications you've received."
        actions={<Button variant="outline" leftIcon={<RefreshCw className="h-4 w-4" />} onClick={() => refetch()}>Refresh</Button>}
      />
      <Card>
        <CardContent>
          {isLoading ? (
            <div className="space-y-2">{Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} className="h-16" />)}</div>
          ) : !data?.length ? (
            <EmptyState icon={<Bell className="h-5 w-5" />} title="You're all caught up" />
          ) : (
            <ul className="divide-y divide-border">
              {data.map((n) => (
                <li key={n.id} className="flex items-start gap-3 py-3">
                  <div className="mt-1 grid h-9 w-9 place-items-center rounded-full bg-muted text-muted-fg">
                    <Bell className="h-4 w-4" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2">
                      <Link to={ROUTES.notification(n.id)} className="font-semibold hover:underline truncate">{n.title}</Link>
                      <Badge tone={tone[n.status as NotificationStatus]}>{n.status}</Badge>
                    </div>
                    <p className="text-sm text-muted-fg line-clamp-2">{n.message}</p>
                    <p className="mt-1 text-xs text-muted-fg">{fromNow(n.createdAt)} · {n.channel}</p>
                  </div>
                  {n.status === 'FAILED' && (
                    <Button size="sm" variant="outline" loading={retry.isPending} onClick={() => retry.mutate(n.id)}>
                      Retry
                    </Button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

