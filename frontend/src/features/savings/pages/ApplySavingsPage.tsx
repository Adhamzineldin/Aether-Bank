import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { Input } from '@shared/ui/Input';
import { Button } from '@shared/ui/Button';
import { Select } from '@shared/ui/Select';
import { ROUTES } from '@app/routes';
import { useAuthStore } from '@stores/authStore';
import { useMyAccounts } from '@features/accounts/hooks';
import { useApplyCertificate } from '../hooks';
import type { CertificateApplication, Decimal, UUID } from '@veld/types';

const schema = z.object({
  accountId: z.string().uuid(),
  principal: z.string(),
  interestRate: z.string(),
  termDays: z.coerce.number().min(7),
  autoRenew: z.coerce.boolean().default(false),
});
type V = z.infer<typeof schema>;

export default function ApplySavingsPage() {
  const customerId = useAuthStore((s) => s.user?.id) || '';
  const accounts = useMyAccounts();
  const navigate = useNavigate();
  const apply = useApplyCertificate();
  const { register, handleSubmit, formState: { errors } } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { interestRate: '4.5', termDays: 180, autoRenew: false as any },
  });

  const submit = (v: V) => {
    const payload: CertificateApplication = {
      id: crypto.randomUUID() as UUID,
      customerId: customerId as UUID,
      accountId: v.accountId as UUID,
      principal: v.principal as Decimal,
      interestRate: v.interestRate as Decimal,
      termDays: v.termDays,
      autoRenew: !!v.autoRenew,
      applicationStatus: 'SUBMITTED',
      submittedAt: new Date().toISOString() as any,
      reviewedAt: new Date().toISOString() as any,
      remarks: '',
    } as CertificateApplication;
    apply.mutate(payload, { onSuccess: () => navigate(ROUTES.savings) });
  };

  return (
    <div>
      <PageHeader title="Open certificate of deposit" back={{ to: ROUTES.savings }} />
      <Card className="max-w-xl">
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit(submit)}>
            <FormField label="Funding account" error={errors.accountId?.message} required>
              <Select {...register('accountId')}>
                <option value="">Select…</option>
                {accounts.data?.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.accountType} · {a.accountNumber}
                  </option>
                ))}
              </Select>
            </FormField>
            <div className="grid gap-4 md:grid-cols-3">
              <FormField label="Principal" required>
                <Input type="number" min="0" step="0.01" {...register('principal')} />
              </FormField>
              <FormField label="Interest rate (%)" required>
                <Input type="number" min="0" step="0.01" {...register('interestRate')} />
              </FormField>
              <FormField label="Term (days)" required>
                <Input type="number" min="7" {...register('termDays')} />
              </FormField>
            </div>
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" {...register('autoRenew')} className="rounded border-border" />
              Auto-renew at maturity
            </label>
            <Button type="submit" loading={apply.isPending}>Submit application</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

