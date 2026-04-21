import { useParams } from 'react-router-dom';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Badge } from '@shared/ui/Badge';
import { ROUTES } from '@app/routes';
import { formatDateTime } from '@shared/utils/date';
import { useNotification } from '../hooks';

export default function NotificationDetailPage() {
  const { id = '' } = useParams();
  const { data, isLoading } = useNotification(id);

  if (isLoading || !data) return <Skeleton className="h-48" />;
  return (
    <div className="space-y-6">
      <PageHeader title={data.title} description={`${data.channel} · ${data.eventType}`} back={{ to: ROUTES.notifications }} />
      <Card>
        <CardContent className="space-y-3">
          <div className="flex items-center gap-2">
            <Badge tone={data.status === 'SENT' ? 'success' : data.status === 'FAILED' ? 'danger' : 'warning'}>{data.status}</Badge>
            <span className="text-xs text-muted-fg">Created {formatDateTime(data.createdAt)}</span>
          </div>
          <p className="whitespace-pre-wrap text-sm">{data.message}</p>
          {data.failedReason && (
            <p className="text-xs text-danger">Failure reason: {data.failedReason}</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

