import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { Input } from '@shared/ui/Input';
import { Select } from '@shared/ui/Select';
import { Textarea } from '@shared/ui/Textarea';
import { Button } from '@shared/ui/Button';
import { LOAN_TYPES, EMPLOYMENT_STATUSES } from '@shared/constants/enums';
import { ROUTES } from '@app/routes';
import { useAuthStore } from '@stores/authStore';
import { useApplyLoan } from '../hooks';
import type { Decimal, EmploymentStatus, LoanApplication, LoanType, UUID } from '@veld/types';

const schema = z.object({
  loanType: z.enum(LOAN_TYPES),
  productId: z.string().uuid('Provide product UUID'),
  requestedAmount: z.string(),
  requestedTenure: z.coerce.number().min(1),
  termYears: z.coerce.number().min(1),
  purpose: z.string().min(3),
  employmentStatus: z.enum(EMPLOYMENT_STATUSES),
  annualIncome: z.string(),
  propertyAddress: z.string().min(2),
  propertyValue: z.string(),
  downPayment: z.string(),
});
type V = z.infer<typeof schema>;

export default function ApplyLoanPage() {
  const navigate = useNavigate();
  const customerId = useAuthStore((s) => s.user?.id) || '';
  const apply = useApplyLoan();
  const { register, handleSubmit, formState: { errors } } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { loanType: 'PERSONAL', employmentStatus: 'EMPLOYED', termYears: 5, requestedTenure: 60 },
  });

  const submit = (v: V) => {
    const payload: LoanApplication = {
      id: crypto.randomUUID() as UUID,
      customerId: customerId as UUID,
      productId: v.productId as UUID,
      loanType: v.loanType as LoanType,
      requestedAmount: v.requestedAmount as Decimal,
      requestedTenure: v.requestedTenure,
      purpose: v.purpose,
      employmentStatus: v.employmentStatus as EmploymentStatus,
      annualIncome: v.annualIncome as Decimal,
      propertyAddress: v.propertyAddress,
      propertyValue: v.propertyValue as Decimal,
      downPayment: v.downPayment as Decimal,
      termYears: v.termYears,
      applicationStatus: 'SUBMITTED',
      submittedAt: new Date().toISOString() as any,
      reviewedAt: new Date().toISOString() as any,
      remarks: '',
    } as LoanApplication;
    apply.mutate(payload, { onSuccess: () => navigate(ROUTES.loans) });
  };

  return (
    <div>
      <PageHeader title="Apply for a loan" back={{ to: ROUTES.loans }} />
      <Card className="max-w-3xl">
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit(submit)}>
            <div className="grid gap-4 md:grid-cols-2">
              <FormField label="Loan type" error={errors.loanType?.message}>
                <Select {...register('loanType')}>{LOAN_TYPES.map((t) => <option key={t}>{t}</option>)}</Select>
              </FormField>
              <FormField label="Product ID" error={errors.productId?.message} required>
                <Input placeholder="UUID" {...register('productId')} />
              </FormField>
              <FormField label="Requested amount" error={errors.requestedAmount?.message} required>
                <Input type="number" min="0" step="0.01" {...register('requestedAmount')} />
              </FormField>
              <FormField label="Tenure (months)" error={errors.requestedTenure?.message} required>
                <Input type="number" min="1" {...register('requestedTenure')} />
              </FormField>
              <FormField label="Term (years)" error={errors.termYears?.message} required>
                <Input type="number" min="1" {...register('termYears')} />
              </FormField>
              <FormField label="Employment status" error={errors.employmentStatus?.message}>
                <Select {...register('employmentStatus')}>{EMPLOYMENT_STATUSES.map((t) => <option key={t}>{t}</option>)}</Select>
              </FormField>
              <FormField label="Annual income" error={errors.annualIncome?.message} required>
                <Input type="number" min="0" {...register('annualIncome')} />
              </FormField>
              <FormField label="Property value" error={errors.propertyValue?.message} required>
                <Input type="number" min="0" {...register('propertyValue')} />
              </FormField>
              <FormField label="Down payment" error={errors.downPayment?.message} required>
                <Input type="number" min="0" {...register('downPayment')} />
              </FormField>
              <FormField label="Property address" error={errors.propertyAddress?.message} required className="md:col-span-2">
                <Input {...register('propertyAddress')} />
              </FormField>
            </div>
            <FormField label="Purpose" error={errors.purpose?.message} required>
              <Textarea rows={3} {...register('purpose')} />
            </FormField>
            <Button type="submit" loading={apply.isPending}>Submit application</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

