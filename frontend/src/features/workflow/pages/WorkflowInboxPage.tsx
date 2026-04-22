import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ListChecks, Check, X, ShieldAlert } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { EmptyState } from '@shared/ui/EmptyState';
import { Badge } from '@shared/ui/Badge';
import { Button } from '@shared/ui/Button';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { Modal } from '@shared/ui/Modal';
import { Textarea } from '@shared/ui/Textarea';
import { ROUTES } from '@app/routes';
import { fromNow } from '@shared/utils/date';
import { useAuthStore } from '@stores/authStore';
import { TaskActionInput, type ApprovalTask, type TaskDecision, type UUID } from '@veld/types';
import { useMyTasks, useTaskAction } from '../hooks';
import { canActOnWorkflowStep } from '../rbac';

export default function WorkflowInboxPage() {
  const { data, isLoading } = useMyTasks();
  const userRoles = useAuthStore((s) => s.user?.roles);
  const [decidingTask, setDecidingTask] = useState<ApprovalTask | null>(null);
  const [pendingDecision, setPendingDecision] = useState<TaskDecision>('APPROVED');

  const openDecision = (task: ApprovalTask, decision: TaskDecision) => {
    if (!canActOnWorkflowStep(task.role, userRoles)) return;
    setDecidingTask(task);
    setPendingDecision(decision);
  };

  return (
    <div>
      <PageHeader title="My approval tasks" description="Workflow tasks assigned to you, ranked by urgency." />
      <Card>
        <CardContent>
          {isLoading ? (
            <Skeleton className="h-32" />
          ) : !data?.length ? (
            <EmptyState icon={<ListChecks className="h-5 w-5" />} title="Inbox zero" description="No tasks assigned to you right now." />
          ) : (
            <Table>
              <THead>
                <tr>
                  <TH>Workflow</TH>
                  <TH>Step</TH>
                  <TH>Role</TH>
                  <TH>Status</TH>
                  <TH>Created</TH>
                  <TH className="text-right">Actions</TH>
                </tr>
              </THead>
              <TBody>
                {data.map((t) => {
                  const isOpen = t.taskStatus !== 'COMPLETED';
                  const canAct = canActOnWorkflowStep(t.role, userRoles);
                  return (
                    <TR key={t.id}>
                      <TD className="font-mono text-xs">{t.workflowId.slice(0, 8)}…</TD>
                      <TD>{t.step}</TD>
                      <TD><Badge>{t.role}</Badge></TD>
                      <TD>
                        <Badge tone={t.taskStatus === 'COMPLETED' ? 'success' : 'warning'}>{t.taskStatus}</Badge>
                      </TD>
                      <TD>{fromNow(t.createdAt)}</TD>
                      <TD>
                        <div className="flex justify-end gap-2 items-center">
                          {isOpen && canAct && (
                            <>
                              <Button
                                size="sm"
                                variant="primary"
                                leftIcon={<Check className="h-3.5 w-3.5" />}
                                onClick={() => openDecision(t, 'APPROVED')}
                              >
                                Approve
                              </Button>
                              <Button
                                size="sm"
                                variant="danger"
                                leftIcon={<X className="h-3.5 w-3.5" />}
                                onClick={() => openDecision(t, 'REJECTED')}
                              >
                                Reject
                              </Button>
                            </>
                          )}
                          {isOpen && !canAct && (
                            <span
                              className="inline-flex items-center gap-1 text-xs text-muted-fg"
                              title={`Requires role: ${t.role}`}
                            >
                              <ShieldAlert className="h-3.5 w-3.5" />
                              Needs {t.role}
                            </span>
                          )}
                          <Link
                            to={ROUTES.workflowDetail(t.workflowId)}
                            className="text-primary text-sm hover:underline self-center"
                          >
                            Open
                          </Link>
                        </div>
                      </TD>
                    </TR>
                  );
                })}
              </TBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <DecideTaskModal
        task={decidingTask}
        decision={pendingDecision}
        onClose={() => setDecidingTask(null)}
      />
    </div>
  );
}

interface DecideTaskModalProps {
  task: ApprovalTask | null;
  decision: TaskDecision;
  onClose: () => void;
}

function DecideTaskModal({ task, decision, onClose }: DecideTaskModalProps) {
  const [comment, setComment] = useState('');
  const userId = useAuthStore((s) => s.user?.id) || '';
  const userRoles = useAuthStore((s) => s.user?.roles);
  const action = useTaskAction(task?.id ?? '');
  const canAct = canActOnWorkflowStep(task?.role, userRoles);

  // Close + clear the modal after a successful decision. useMutation's
  // onSuccess already fires the toast and invalidates the task list.
  useEffect(() => {
    if (!action.isSuccess) return;
    setComment('');
    action.reset();
    onClose();
  }, [action, onClose]);

  // Reset comment when the caller switches to a different task.
  useEffect(() => {
    setComment('');
  }, [task?.id]);

  if (!task) return null;

  const title = decision === 'APPROVED' ? 'Approve task' : 'Reject task';
  const verb = decision === 'APPROVED' ? 'Approve' : 'Reject';
  const tone = decision === 'APPROVED' ? 'primary' : 'danger';

  const submit = () => {
    if (!userId || !canAct) return;
    action.mutate(
      new TaskActionInput({
        employeeId: userId as UUID,
        decision,
        comment: comment.trim() || undefined,
      }),
    );
  };

  return (
    <Modal
      open={!!task}
      onClose={() => {
        setComment('');
        action.reset();
        onClose();
      }}
      title={title}
      description={`Step ${task.step} · ${task.role} · workflow ${String(task.workflowId).slice(0, 8)}`}
      footer={
        <>
          <Button
            variant="ghost"
            onClick={() => {
              setComment('');
              action.reset();
              onClose();
            }}
            disabled={action.isPending}
          >
            Cancel
          </Button>
          <Button
            variant={tone as 'primary' | 'danger'}
            onClick={submit}
            loading={action.isPending}
            disabled={!userId || !canAct}
          >
            {verb}
          </Button>
        </>
      }
    >
      <div className="space-y-3">
        {!canAct && (
          <div className="rounded-md border border-warning/40 bg-warning/10 p-3 text-sm text-warning">
            <ShieldAlert className="inline h-4 w-4 mr-1" />
            You don't hold the required role ({task.role}) for this step.
          </div>
        )}
        <label className="text-sm font-medium">
          Comment {decision === 'REJECTED' ? '' : <span className="text-muted-fg font-normal">(optional)</span>}
        </label>
        <Textarea
          rows={3}
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder={
            decision === 'APPROVED'
              ? 'Add a short note for the audit trail (optional).'
              : 'Explain why this is being rejected.'
          }
        />
      </div>
    </Modal>
  );
}
