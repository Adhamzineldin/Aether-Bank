import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { http } from '@lib/axios';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { Badge } from '@shared/ui/Badge';
import { Alert } from '@shared/ui/Alert';
import { ROUTES } from '@app/routes';

interface UserDto { id: string; userName: string; email: string; role: string }

export default function UserDetailPage() {
  const { id = '' } = useParams();
  const q = useQuery({
    queryKey: ['user', id],
    queryFn: () => http.get<UserDto>(`/api/auth/users/${id}`).then((r) => r.data),
  });

  if (q.isLoading) return <Skeleton className="h-32" />;
  if (q.isError || !q.data) {
    return (
      <div className="space-y-4">
        <PageHeader title="User" back={{ to: ROUTES.adminUsers }} />
        <Alert tone="warning">User endpoint not available. Check IAM service exposure.</Alert>
      </div>
    );
  }

  const u = q.data;
  return (
    <div className="space-y-6">
      <PageHeader title={u.userName} description={u.email} back={{ to: ROUTES.adminUsers }} />
      <Card>
        <CardContent className="grid gap-3 md:grid-cols-2 text-sm">
          <div><span className="text-muted-fg">ID:</span> <code className="font-mono">{u.id}</code></div>
          <div><span className="text-muted-fg">Role:</span> <Badge>{u.role}</Badge></div>
          <div><span className="text-muted-fg">Email:</span> {u.email}</div>
          <div><span className="text-muted-fg">Username:</span> {u.userName}</div>
        </CardContent>
      </Card>
    </div>
  );
}
