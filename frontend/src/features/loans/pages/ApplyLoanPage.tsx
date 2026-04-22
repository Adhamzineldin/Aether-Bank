import { useEffect, useMemo } from 'react';
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
import { Skeleton } from '@shared/ui/Skeleton';
import { EMPLOYMENT_STATUSES } from '@shared/constants/enums';
import { ROUTES } from '@app/routes';
import { useAuthStore } from '@stores/authStore';
import { useMyAccounts } from '@features/accounts/hooks';
import { useApplyLoan, useLoanProducts } from '../hooks';
import type { Decimal, EmploymentStatus, LoanApplication, LoanType, UUID } from '@veld/types';

/**
 * Customers choose a real product from the catalog (e.g. "Personal Loan",
 * "Mortgage") — we submit its UUID behind the scenes. No more hand-typing
 * product UUIDs.
 */
const schema = z.object({
  productId: z.string().uuid('Choose a product'),
  accountId: z.string().uuid('Select a disbursement account'),
  requestedAmount: z.string(),
  requestedTenure: z.coerce.number().min(1),
  termYears: z.coerce.number().min(1),
  purpose: z.string().min(3),
  employmentStatus: z.enum(EMPLOYMENT_STATUSES),
  annualIncome: z.string(),
  propertyAddress: z.string().optional(),
  propertyValue: z.string().optional(),
  downPayment: z.string().optional(),
});
type V = z.infer<typeof schema>;

export default function ApplyLoanPage() {
  const navigate = useNavigate();
  const customerId = useAuthStore((s) => s.user?.id) || '';
  const products = useLoanProducts();
  const accounts = useMyAccounts();
  const apply = useApplyLoan();

  const { register, handleSubmit, setValue, watch, formState: { errors } } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { employmentStatus: 'EMPLOYED', termYears: 5, requestedTenure: 60 },
  });

  const productId = watch('productId');
  const selectedProduct = useMemo(
    () => products.data?.find((p) => p.id === productId) || null,
    [products.data, productId],
  );

  // Auto-fill sensible defaults from the selected product.
  useEffect(() => {
    if (!selectedProduct) return;
    setValue('requestedTenure', selectedProduct.defaultTenureMonths);
    setValue('termYears', Math.max(1, Math.round(selectedProduct.defaultTenureMonths / 12)));
  }, [selectedProduct, setValue]);

  const submit = (v: V) => {
    if (!selectedProduct) return;
    const selectedAccount = accounts.data?.find((a) => a.id === v.accountId);
    const payload: LoanApplication = {
      id: crypto.randomUUID() as UUID,
      customerId: customerId as UUID,
      accountId: v.accountId as UUID,
      productId: v.productId as UUID,
      loanType: selectedProduct.loanType as LoanType,
      requestedAmount: v.requestedAmount as Decimal,
      currency: (selectedAccount?.currency || 'USD') as string,
      requestedTenure: v.requestedTenure,
      purpose: v.purpose,
      employmentStatus: v.employmentStatus as EmploymentStatus,
      annualIncome: v.annualIncome as Decimal,
      propertyAddress: v.propertyAddress || '',
      propertyValue: (v.propertyValue || '0') as Decimal,
      downPayment: (v.downPayment || '0') as Decimal,
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
          {products.isLoading ? (
            <Skeleton className="h-48" />
          ) : (
            <form className="space-y-4" onSubmit={handleSubmit(submit)}>
              <FormField label="Product" error={errors.productId?.message} required>
                <Select {...register('productId')}>
                  <option value="">Choose a loan product…</option>
                  {products.data?.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name} · {(Number(p.baseAnnualRate) * 100).toFixed(2)}% APR
                    </option>
                  ))}
                </Select>
              </FormField>

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

              {selectedProduct && (
                <div className="rounded-md border border-border bg-muted/20 p-3 text-xs text-muted-fg grid gap-1 md:grid-cols-3">
                  <div>Tenure: {selectedProduct.minimumTenureMonths}–{selectedProduct.maximumTenureMonths} mo</div>
                  <div>Principal: {Number(selectedProduct.minimumPrincipal).toLocaleString()} – {Number(selectedProduct.maximumPrincipal).toLocaleString()}</div>
                  <div>Repayment: {selectedProduct.repaymentMethod}</div>
                </div>
              )}

              <div className="grid gap-4 md:grid-cols-2">
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
                  <Select {...register('employmentStatus')}>
                    {EMPLOYMENT_STATUSES.map((t) => <option key={t}>{t}</option>)}
                  </Select>
                </FormField>
                <FormField label="Annual income" error={errors.annualIncome?.message} required>
                  <Input type="number" min="0" {...register('annualIncome')} />
                </FormField>
                {selectedProduct?.loanType === 'MORTGAGE' && (
                  <>
                    <FormField label="Property value" required>
                      <Input type="number" min="0" {...register('propertyValue')} />
                    </FormField>
                    <FormField label="Down payment" required>
                      <Input type="number" min="0" {...register('downPayment')} />
                    </FormField>
                    <FormField label="Property address" className="md:col-span-2" required>
                      <Input {...register('propertyAddress')} />
                    </FormField>
                  </>
                )}
              </div>

              <FormField label="Purpose" error={errors.purpose?.message} required>
                <Textarea rows={3} {...register('purpose')} placeholder="What will you use this loan for?" />
              </FormField>

              <Button type="submit" loading={apply.isPending} disabled={!selectedProduct}>
                Submit application
              </Button>
            </form>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
