import { useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ArrowRight } from 'lucide-react';
import { Card, CardContent } from '@shared/ui/Card';
import { FormField } from '@shared/ui/FormField';
import { Input } from '@shared/ui/Input';
import { Select } from '@shared/ui/Select';
import { Button } from '@shared/ui/Button';
import { PageHeader } from '@shared/ui/PageHeader';
import { CURRENCIES } from '@shared/constants/enums';
import { useAuthStore } from '@stores/authStore';
import { ROUTES } from '@app/routes';
import { useMyAccounts } from '@features/accounts/hooks';
import { useTransfer } from '../hooks';
import type { TransferRequest, TransactionType, UUID, Decimal } from '@veld/types';

const schema = z.object({
  sourceAccountId: z.string().uuid('Choose source account'),
  destinationAccountId: z.string().uuid('Provide destination account'),
  amount: z.string().refine((v) => Number(v) > 0, 'Amount must be > 0'),
  currency: z.string().min(3),
  type: z.enum(['TRANSFER', 'INTERNAL_TRANSFER', 'BILL_PAYMENT']),
});
type Values = z.infer<typeof schema>;

export default function TransferPage() {
  const userId = useAuthStore((s) => s.user?.id) || '';
  const accounts = useMyAccounts();
  const navigate = useNavigate();
  const [search] = useSearchParams();
  const transfer = useTransfer();

  const { register, handleSubmit, formState: { errors } } = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: {
      sourceAccountId: search.get('source') || accounts.data?.[0]?.account.id || '',
      currency: 'USD',
      type: 'TRANSFER',
    },
  });

  const submit = (v: Values) => {
    const payload: TransferRequest = {
      idempotencyKey: crypto.randomUUID(),
      sourceAccountId: v.sourceAccountId as UUID,
      destinationAccountId: v.destinationAccountId as UUID,
      amount: v.amount as Decimal,
      currency: v.currency,
      sourceCurrency: v.currency,
      destinationCurrency: v.currency,
      type: v.type as TransactionType,
      initiatedByUserId: userId as UUID,
    } as TransferRequest;
    transfer.mutate(payload, { onSuccess: () => navigate(ROUTES.transactions) });
  };

  return (
    <div>
      <PageHeader title="New transfer" description="Send money between your accounts or to other Aether customers." />
      <Card className="max-w-2xl">
        <CardContent>
          <form onSubmit={handleSubmit(submit)} className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <FormField label="From account" error={errors.sourceAccountId?.message} required>
                <Select {...register('sourceAccountId')}>
                  <option value="">Select…</option>
                  {accounts.data?.map((a) => (
                    <option key={a.account.id} value={a.account.id}>{a.account.accountType} · {a.account.accountNumber}</option>
                  ))}
                </Select>
              </FormField>
              <FormField label="To account ID" error={errors.destinationAccountId?.message} required>
                <Input placeholder="UUID of destination account" {...register('destinationAccountId')} />
              </FormField>
            </div>
            <div className="grid gap-4 md:grid-cols-3">
              <FormField label="Amount" error={errors.amount?.message} required>
                <Input type="number" step="0.01" min="0" placeholder="0.00" {...register('amount')} />
              </FormField>
              <FormField label="Currency" required>
                <Select {...register('currency')}>
                  {CURRENCIES.map((c) => <option key={c} value={c}>{c}</option>)}
                </Select>
              </FormField>
              <FormField label="Type">
                <Select {...register('type')}>
                  <option value="TRANSFER">Transfer</option>
                  <option value="INTERNAL_TRANSFER">Internal</option>
                  <option value="BILL_PAYMENT">Bill payment</option>
                </Select>
              </FormField>
            </div>
            <Button type="submit" loading={transfer.isPending} rightIcon={<ArrowRight className="h-4 w-4" />}>
              Send transfer
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

