import { useParams } from 'react-router-dom';
import { useState } from 'react';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Stepper } from '@shared/ui/Stepper';
import { Button } from '@shared/ui/Button';
import { Textarea } from '@shared/ui/Textarea';
import { Badge } from '@shared/ui/Badge';
import { useAuthStore } from '@stores/authStore';
import { ROUTES } from '@app/routes';
import { useWorkflow } from '../hooks';
import { useMutation } from '@tanstack/react-query';
import { useVeld } from '@shared/hooks/useVeld';
import { toast } from 'sonner';
import type { TaskActionInput, UUID } from '@veld/types';

export default function WorkflowDetailPage() {
  const { id = '' } = useParams();
  const wf = useWorkflow(id);
  const userId = useAuthStore((s) => s.user?.id) || '';
  const [comment, setComment] = useState('');
  const veld = useVeld();

  const decide = useMutation({
    mutationFn: (decision: 'APPROVED' | 'REJECTED') =>
      // We act on the *workflow* — the backend resolves the next pending task.
      veld.workflow.decideTask(id, { employeeId: userId as UUID, decision, comment } as TaskActionInput),
    onSuccess: () => { toast.success('Decision recorded'); wf.refetch(); },
  });

  if (wf.isLoading || !wf.data) return <Skeleton className="h-64" />;
  const w = wf.data;

  return (
    <div className="space-y-6">
      <PageHeader title={`Workflow · ${w.entityType}`} description={`Entity ${w.entityId}`} back={{ to: ROUTES.workflow }} />
      <Card>
        <CardContent>
          <div className="flex items-center justify-between">
            <Stepper steps={w.steps.map((s, i) => ({ id: String(s.id), label: `${s.role} · ${s.action}` }))} currentIndex={w.currentStep} />
            <Badge tone={w.status === 'APPROVED' ? 'success' : w.status === 'REJECTED' ? 'danger' : 'warning'}>{w.status}</Badge>
          </div>
        </CardContent>
      </Card>
      {w.status === 'PENDING' || w.status === 'IN_PROGRESS' ? (
        <Card>
          <CardContent className="space-y-3">
            <h3 className="text-sm font-semibold">Decide</h3>
            <Textarea rows={3} placeholder="Optional comment…" value={comment} onChange={(e) => setComment(e.target.value)} />
            <div className="flex gap-2">
              <Button variant="primary" loading={decide.isPending} onClick={() => decide.mutate('APPROVED')}>Approve</Button>
              <Button variant="danger" loading={decide.isPending} onClick={() => decide.mutate('REJECTED')}>Reject</Button>
            </div>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}


