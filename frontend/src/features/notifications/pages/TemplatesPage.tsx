import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { FormField } from '@shared/ui/FormField';
import { Input } from '@shared/ui/Input';
import { Textarea } from '@shared/ui/Textarea';
import { Select } from '@shared/ui/Select';
import { Button } from '@shared/ui/Button';
import { Badge } from '@shared/ui/Badge';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { NOTIFICATION_CHANNELS, NOTIFICATION_EVENTS } from '@shared/constants/enums';
import { useTemplates, useCreateTemplate, useDeactivateTemplate } from '../hooks';
import type { CreateTemplateInput, NotificationChannel, NotificationEventType } from '@veld/types';

const schema = z.object({
  eventType: z.enum(NOTIFICATION_EVENTS),
  channel: z.enum(NOTIFICATION_CHANNELS),
  titleTemplate: z.string().min(1),
  bodyTemplate: z.string().min(1),
});
type V = z.infer<typeof schema>;

export default function TemplatesPage() {
  const list = useTemplates();
  const create = useCreateTemplate();
  const deactivate = useDeactivateTemplate();
  const { register, handleSubmit, reset, formState: { errors } } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { eventType: 'TRANSFER_COMPLETED', channel: 'EMAIL' },
  });

  const submit = (v: V) => {
    create.mutate(
      { ...v, eventType: v.eventType as NotificationEventType, channel: v.channel as NotificationChannel } as CreateTemplateInput,
      { onSuccess: () => reset() },
    );
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Notification templates" description="Manage transactional email/SMS/push templates." />
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardContent>
            <h3 className="mb-3 text-sm font-semibold">Create template</h3>
            <form className="space-y-3" onSubmit={handleSubmit(submit)}>
              <div className="grid gap-3 md:grid-cols-2">
                <FormField label="Event">
                  <Select {...register('eventType')}>{NOTIFICATION_EVENTS.map((e) => <option key={e}>{e}</option>)}</Select>
                </FormField>
                <FormField label="Channel">
                  <Select {...register('channel')}>{NOTIFICATION_CHANNELS.map((c) => <option key={c}>{c}</option>)}</Select>
                </FormField>
              </div>
              <FormField label="Title template" error={errors.titleTemplate?.message} required>
                <Input {...register('titleTemplate')} />
              </FormField>
              <FormField label="Body template" error={errors.bodyTemplate?.message} required>
                <Textarea rows={5} {...register('bodyTemplate')} />
              </FormField>
              <Button type="submit" loading={create.isPending}>Create template</Button>
            </form>
          </CardContent>
        </Card>
        <Card>
          <CardContent>
            <h3 className="mb-3 text-sm font-semibold">Existing templates</h3>
            {list.isLoading ? (
              <Skeleton className="h-32" />
            ) : !list.data?.length ? (
              <p className="text-sm text-muted-fg">No templates yet.</p>
            ) : (
              <Table>
                <THead>
                  <tr><TH>Event</TH><TH>Channel</TH><TH>Active</TH><TH><span className="sr-only">Actions</span></TH></tr>
                </THead>
                <TBody>
                  {list.data.map((t) => (
                    <TR key={t.id}>
                      <TD>{t.eventType}</TD>
                      <TD>{t.channel}</TD>
                      <TD><Badge tone={t.isActive ? 'success' : 'neutral'}>{t.isActive ? 'Active' : 'Inactive'}</Badge></TD>
                      <TD className="text-right">
                        {t.isActive && (
                          <Button size="sm" variant="outline" onClick={() => deactivate.mutate(t.id)}>Deactivate</Button>
                        )}
                      </TD>
                    </TR>
                  ))}
                </TBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}


