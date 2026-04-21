import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { Input } from '@shared/ui/Input';
import { Button } from '@shared/ui/Button';
import { Tabs } from '@shared/ui/Tabs';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '@stores/authStore';
import { ROUTES } from '@app/routes';
import { useUpdateProfile } from '../hooks';

const schema = z.object({
  userName: z.string().min(2),
  email: z.string().email(),
});
type V = z.infer<typeof schema>;

export default function ProfilePage() {
  const user = useAuthStore((s) => s.user);
  const update = useUpdateProfile();
  const [tab, setTab] = useState('account');
  const { register, handleSubmit, formState: { errors } } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { userName: user?.userName || '', email: user?.email || '' },
  });

  return (
    <div className="space-y-6">
      <PageHeader title="Profile" description="Manage your personal information and security settings." />
      <Tabs
        items={[
          { id: 'account', label: 'Account' },
          { id: 'security', label: 'Security' },
          { id: 'preferences', label: 'Preferences' },
        ]}
        activeId={tab}
        onChange={(id) => setTab(id)}
      />
      {tab === 'account' && (
        <Card className="max-w-xl">
          <CardContent>
            <form className="space-y-4" onSubmit={handleSubmit((v) => update.mutate(v))}>
              <FormField label="Username" error={errors.userName?.message} required>
                <Input {...register('userName')} />
              </FormField>
              <FormField label="Email" error={errors.email?.message} required>
                <Input type="email" {...register('email')} />
              </FormField>
              <Button type="submit" loading={update.isPending}>Save changes</Button>
            </form>
          </CardContent>
        </Card>
      )}
      {tab === 'security' && (
        <Card className="max-w-xl">
          <CardContent>
            <p className="text-sm text-muted-fg">Change your password, manage trusted devices and 2FA.</p>
            <Link to={ROUTES.security} className="mt-3 inline-block text-primary text-sm hover:underline">Open security settings</Link>
          </CardContent>
        </Card>
      )}
      {tab === 'preferences' && (
        <Card className="max-w-xl">
          <CardContent>
            <p className="text-sm text-muted-fg">Theme, language and notification preferences.</p>
            <Link to={ROUTES.preferences} className="mt-3 inline-block text-primary text-sm hover:underline">Open preferences</Link>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

