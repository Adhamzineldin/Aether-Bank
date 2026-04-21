import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { PasswordInput } from '@shared/ui/PasswordInput';
import { Button } from '@shared/ui/Button';
import { Alert } from '@shared/ui/Alert';
import { ROUTES } from '@app/routes';
import { useChangePassword, useDeleteAccount } from '../hooks';

const schema = z.object({
  oldPassword: z.string().min(8),
  newPassword: z.string().min(8),
  confirm: z.string().min(8),
}).refine((v) => v.newPassword === v.confirm, { path: ['confirm'], message: 'Passwords do not match' });
type V = z.infer<typeof schema>;

export default function SecurityPage() {
  const change = useChangePassword();
  const del = useDeleteAccount();
  const { register, handleSubmit, formState: { errors } } = useForm<V>({ resolver: zodResolver(schema) });

  return (
    <div className="space-y-6">
      <PageHeader title="Security" description="Password, sessions and account deletion." back={{ to: ROUTES.profile }} />
      <Card className="max-w-xl">
        <CardContent>
          <h3 className="text-sm font-semibold mb-3">Change password</h3>
          <form className="space-y-3" onSubmit={handleSubmit((v) => change.mutate({ oldPassword: v.oldPassword, newPassword: v.newPassword }))}>
            <FormField label="Current password" required error={errors.oldPassword?.message}>
              <PasswordInput {...register('oldPassword')} />
            </FormField>
            <FormField label="New password" required error={errors.newPassword?.message}>
              <PasswordInput {...register('newPassword')} />
            </FormField>
            <FormField label="Confirm new password" required error={errors.confirm?.message}>
              <PasswordInput {...register('confirm')} />
            </FormField>
            <Button type="submit" loading={change.isPending}>Update password</Button>
          </form>
        </CardContent>
      </Card>
      <Card className="max-w-xl border-danger/30">
        <CardContent>
          <h3 className="text-sm font-semibold text-danger mb-2">Danger zone</h3>
          <Alert tone="danger" title="Delete account">
            This will permanently remove your data. This action cannot be undone.
          </Alert>
          <Button variant="danger" className="mt-3" loading={del.isPending} onClick={() => {
            if (confirm('Are you sure? This is permanent.')) del.mutate();
          }}>
            Delete my account
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}

