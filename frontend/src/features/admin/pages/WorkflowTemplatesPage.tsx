import { useMemo, useState } from 'react';
import { GitBranch, Plus, Trash2, ArrowUp, ArrowDown, Pencil, Save, X } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Button } from '@shared/ui/Button';
import { FormField } from '@shared/ui/FormField';
import { Input } from '@shared/ui/Input';
import { Select } from '@shared/ui/Select';
import { Badge } from '@shared/ui/Badge';
import { Skeleton } from '@shared/ui/Skeleton';
import { EmptyState } from '@shared/ui/EmptyState';
import { Modal } from '@shared/ui/Modal';
import { Alert } from '@shared/ui/Alert';
import {
  useWorkflowTemplates,
  useCreateWorkflowTemplate,
  useUpdateWorkflowTemplate,
  useDeleteWorkflowTemplate,
  type WorkflowTemplate,
  type TemplatePayload,
} from '../templates';
import { useRoles } from '../hooks';

// Actions supported by the workflow engine today (see StepAction enum).
const ACTIONS = ['APPROVE_LOAN', 'APPROVE_MORTGAGE', 'APPROVE_CERTIFICATE'] as const;

// Entity types that can be driven off a workflow template.
const ENTITY_TYPES = ['LOAN', 'MORTGAGE', 'CERTIFICATE'] as const;

// Approval roles the task router actually understands. Anything outside this
// set is accepted by the API but will never have a matching IAM user, so we
// nudge admins toward the canonical three + any extras already in the IAM.
const CORE_WF_ROLES = ['RISK', 'MANAGER', 'DIRECTOR'];

export default function WorkflowTemplatesPage() {
  const { data, isLoading } = useWorkflowTemplates();
  const [editing, setEditing] = useState<WorkflowTemplate | null>(null);
  const [creating, setCreating] = useState(false);

  const templates = data ?? [];

  return (
    <div>
      <PageHeader
        title="Workflow templates"
        description="Define which employee roles must approve each entity type and in what order."
        actions={
          <Button leftIcon={<Plus className="h-4 w-4" />} onClick={() => setCreating(true)}>
            New template
          </Button>
        }
      />

      <Alert tone="info" className="mb-4">
        Changes apply to <strong>future</strong> applications only. In-flight workflows keep their original
        step definitions so approvals that are already underway don't shift mid-process.
      </Alert>

      {isLoading ? (
        <Skeleton className="h-32" />
      ) : !templates.length ? (
        <Card>
          <CardContent>
            <EmptyState
              icon={<GitBranch className="h-5 w-5" />}
              title="No workflow templates yet"
              description="Create one to route approvals across Risk, Manager and Director roles."
            />
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {templates.map((t) => (
            <TemplateCard key={t.id} template={t} onEdit={() => setEditing(t)} />
          ))}
        </div>
      )}

      {creating && <TemplateEditor onClose={() => setCreating(false)} />}
      {editing && <TemplateEditor template={editing} onClose={() => setEditing(null)} />}
    </div>
  );
}

function TemplateCard({ template, onEdit }: { template: WorkflowTemplate; onEdit: () => void }) {
  const remove = useDeleteWorkflowTemplate();

  return (
    <Card>
      <CardContent className="space-y-3">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-xs uppercase tracking-wide text-muted-fg">Entity</p>
            <p className="text-lg font-semibold">{template.entityType}</p>
          </div>
          <div className="flex items-center gap-2">
            <Button size="sm" variant="outline" leftIcon={<Pencil className="h-3.5 w-3.5" />} onClick={onEdit}>
              Edit
            </Button>
            <Button
              size="sm"
              variant="ghost"
              leftIcon={<Trash2 className="h-3.5 w-3.5" />}
              loading={remove.isPending}
              onClick={() => {
                if (window.confirm(`Delete ${template.entityType} workflow template?`)) {
                  remove.mutate(template.id);
                }
              }}
            >
              <span className="sr-only">Delete</span>
            </Button>
          </div>
        </div>
        <ol className="space-y-1.5">
          {template.steps.map((s, i) => (
            <li key={s.id || i} className="flex items-center gap-2 rounded-md border border-border bg-muted/30 px-3 py-2">
              <span className="grid h-6 w-6 place-items-center rounded-full bg-primary/10 text-xs font-semibold text-primary">
                {i + 1}
              </span>
              <Badge tone="info">{s.role}</Badge>
              <span className="text-xs text-muted-fg">{s.action}</span>
            </li>
          ))}
        </ol>
      </CardContent>
    </Card>
  );
}

interface EditorStep {
  id?: string;
  role: string;
  action: string;
}

function TemplateEditor({
  template,
  onClose,
}: {
  template?: WorkflowTemplate;
  onClose: () => void;
}) {
  const roles = useRoles();
  const create = useCreateWorkflowTemplate();
  const update = useUpdateWorkflowTemplate(template?.id || '');

  const [entityType, setEntityType] = useState(template?.entityType || 'LOAN');
  const [steps, setSteps] = useState<EditorStep[]>(
    template?.steps?.map((s) => ({ id: s.id, role: s.role, action: s.action })) ?? [
      { role: 'RISK', action: 'APPROVE_LOAN' },
    ],
  );

  const roleOptions = useMemo(() => {
    const extra = (roles.data ?? []).filter((r) => !CORE_WF_ROLES.includes(r));
    return [...CORE_WF_ROLES, ...extra];
  }, [roles.data]);

  const setStep = (i: number, patch: Partial<EditorStep>) =>
    setSteps((prev) => prev.map((s, idx) => (idx === i ? { ...s, ...patch } : s)));

  const move = (i: number, dir: -1 | 1) => {
    const j = i + dir;
    if (j < 0 || j >= steps.length) return;
    setSteps((prev) => {
      const next = [...prev];
      [next[i], next[j]] = [next[j], next[i]];
      return next;
    });
  };

  const remove = (i: number) => setSteps((prev) => prev.filter((_, idx) => idx !== i));
  const add = () =>
    setSteps((prev) => [
      ...prev,
      { role: CORE_WF_ROLES[0], action: guessAction(entityType) },
    ]);

  const submit = () => {
    const payload: TemplatePayload = {
      entityType: entityType.trim().toUpperCase(),
      steps: steps.map((s) => ({ id: s.id, role: s.role, action: s.action })),
    };
    if (!payload.entityType) return;
    if (!payload.steps.length) return;
    const op = template ? update.mutateAsync(payload) : create.mutateAsync(payload);
    op.then(onClose).catch(() => {});
  };

  const pending = create.isPending || update.isPending;

  return (
    <Modal
      open
      onClose={onClose}
      size="lg"
      title={template ? `Edit ${template.entityType} template` : 'New workflow template'}
      description="Define the ordered sequence of role approvals required for this entity type."
      footer={
        <>
          <Button variant="ghost" leftIcon={<X className="h-4 w-4" />} onClick={onClose}>
            Cancel
          </Button>
          <Button leftIcon={<Save className="h-4 w-4" />} loading={pending} onClick={submit}>
            Save
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        <FormField label="Entity type" required>
          {template ? (
            <Input value={entityType} onChange={(e) => setEntityType(e.target.value.toUpperCase())} />
          ) : (
            <Select value={entityType} onChange={(e) => setEntityType(e.target.value)}>
              {ENTITY_TYPES.map((e) => (
                <option key={e} value={e}>
                  {e}
                </option>
              ))}
            </Select>
          )}
        </FormField>

        <div>
          <div className="flex items-center justify-between">
            <label className="text-sm font-medium">Approval steps</label>
            <Button size="sm" variant="outline" leftIcon={<Plus className="h-3.5 w-3.5" />} onClick={add}>
              Add step
            </Button>
          </div>
          <div className="mt-2 space-y-2">
            {steps.map((s, i) => (
              <div key={i} className="grid grid-cols-[auto,1fr,1fr,auto] items-end gap-2 rounded-lg border border-border p-3">
                <div className="grid h-9 w-9 place-items-center rounded-md bg-primary/10 text-sm font-semibold text-primary">
                  {i + 1}
                </div>
                <FormField label="Role">
                  <Select value={s.role} onChange={(e) => setStep(i, { role: e.target.value })}>
                    {roleOptions.map((r) => (
                      <option key={r} value={r}>
                        {r}
                      </option>
                    ))}
                  </Select>
                </FormField>
                <FormField label="Action">
                  <Select value={s.action} onChange={(e) => setStep(i, { action: e.target.value })}>
                    {ACTIONS.map((a) => (
                      <option key={a} value={a}>
                        {a}
                      </option>
                    ))}
                  </Select>
                </FormField>
                <div className="flex items-center gap-1 pb-1">
                  <button
                    type="button"
                    title="Move up"
                    className="rounded p-1.5 text-muted-fg hover:bg-muted disabled:opacity-30"
                    disabled={i === 0}
                    onClick={() => move(i, -1)}
                  >
                    <ArrowUp className="h-4 w-4" />
                  </button>
                  <button
                    type="button"
                    title="Move down"
                    className="rounded p-1.5 text-muted-fg hover:bg-muted disabled:opacity-30"
                    disabled={i === steps.length - 1}
                    onClick={() => move(i, 1)}
                  >
                    <ArrowDown className="h-4 w-4" />
                  </button>
                  <button
                    type="button"
                    title="Remove step"
                    className="rounded p-1.5 text-danger hover:bg-danger/10 disabled:opacity-30"
                    disabled={steps.length === 1}
                    onClick={() => remove(i)}
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </Modal>
  );
}

function guessAction(entity: string): string {
  switch (entity.toUpperCase()) {
    case 'MORTGAGE':
      return 'APPROVE_MORTGAGE';
    case 'CERTIFICATE':
      return 'APPROVE_CERTIFICATE';
    default:
      return 'APPROVE_LOAN';
  }
}
