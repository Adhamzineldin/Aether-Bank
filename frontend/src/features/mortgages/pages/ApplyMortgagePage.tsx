import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { Input } from '@shared/ui/Input';
import { Select } from '@shared/ui/Select';
import { Button } from '@shared/ui/Button';
import { EMPLOYMENT_STATUSES } from '@shared/constants/enums';
import { ROUTES } from '@app/routes';
import { useAuthStore } from '@stores/authStore';
import { useMyAccounts } from '@features/accounts/hooks';
import { useApplyMortgage } from '../hooks';
import type { Decimal, EmploymentStatus, MortgageApplication, UUID } from '@veld/types';

const schema = z.object({
  accountId: z.string().uuid('Select a disbursement account'),
  propertyAddress: z.string().min(2),
  propertyValue: z.string(),
  downPayment: z.string(),
  requestedAmount: z.string(),
  termYears: z.coerce.number().min(1),
  employmentStatus: z.enum(EMPLOYMENT_STATUSES),
  annualIncome: z.string(),
  creditScore: z.coerce.number().optional(),
});
type V = z.infer<typeof schema>;

export default function ApplyMortgagePage() {
  const customerId = useAuthStore((s) => s.user?.id) || '';
  const accounts = useMyAccounts();
  const navigate = useNavigate();
  const apply = useApplyMortgage();
  const { register, handleSubmit, formState: { errors } } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { employmentStatus: 'EMPLOYED', termYears: 30 },
  });

  const submit = (v: V) => {
    const payload: MortgageApplication = {
      id: crypto.randomUUID() as UUID,
      customerId: customerId as UUID,
      accountId: v.accountId as UUID,
      propertyAddress: v.propertyAddress,
      propertyValue: v.propertyValue as Decimal,
      downPayment: v.downPayment as Decimal,
      requestedAmount: v.requestedAmount as Decimal,
      termYears: v.termYears,
      employmentStatus: v.employmentStatus as EmploymentStatus,
      annualIncome: v.annualIncome as Decimal,
      creditScore: v.creditScore,
      applicationStatus: 'SUBMITTED',
      submittedAt: new Date().toISOString() as any,
    } as MortgageApplication;
    apply.mutate(payload, { onSuccess: () => navigate(ROUTES.mortgages) });
  };

  return (
    <div>
      <PageHeader title="Apply for mortgage" back={{ to: ROUTES.mortgages }} />
      <Card className="max-w-2xl">
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit(submit)}>
            <FormField label="Disbursement account" error={errors.accountId?.message} required>
              <Select {...register('accountId')}>
                <option value="">Select the account to receive the funds…</option>
                {accounts.data?.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.accountType} · {a.accountNumber} ({a.currency})
                  </option>
                ))}
              </Select>
            </FormField>
            <FormField label="Property address" error={errors.propertyAddress?.message} required>
              <Input {...register('propertyAddress')} />
            </FormField>
            <div className="grid gap-4 md:grid-cols-3">
              <FormField label="Property value" required><Input type="number" min="0" {...register('propertyValue')} /></FormField>
              <FormField label="Down payment" required><Input type="number" min="0" {...register('downPayment')} /></FormField>
              <FormField label="Requested loan" required><Input type="number" min="0" {...register('requestedAmount')} /></FormField>
              <FormField label="Term (years)" required><Input type="number" min="1" {...register('termYears')} /></FormField>
              <FormField label="Annual income" required><Input type="number" min="0" {...register('annualIncome')} /></FormField>
              <FormField label="Credit score"><Input type="number" min="300" max="850" {...register('creditScore')} /></FormField>
            </div>
            <FormField label="Employment status" required>
              <Select {...register('employmentStatus')}>{EMPLOYMENT_STATUSES.map((t) => <option key={t}>{t}</option>)}</Select>
            </FormField>
            <Button type="submit" loading={apply.isPending}>Submit application</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

