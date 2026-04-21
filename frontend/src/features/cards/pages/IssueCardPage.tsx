import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { Select } from '@shared/ui/Select';
import { Input } from '@shared/ui/Input';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';
import { useAuthStore } from '@stores/authStore';
import { useMyAccounts } from '@features/accounts/hooks';
import { useIssueCard } from '../hooks';

const schema = z
  .object({
    cardType: z.enum(['DEBIT', 'CREDIT']),
    cardNetwork: z.enum(['VISA', 'MASTERCARD', 'AMEX']),
    accountId: z.string().optional(),
    currency: z.string().optional(),
    creditLimit: z.string().optional(),
  })
  .refine((v) => v.cardType === 'CREDIT' || !!v.accountId, {
    message: 'Pick an account to link this debit card to',
    path: ['accountId'],
  });

type V = z.infer<typeof schema>;

export default function IssueCardPage() {
  const navigate = useNavigate();
  const customerId = useAuthStore((s) => s.user?.id) || '';
  const accounts = useMyAccounts();
  const issue = useIssueCard();

  const { register, handleSubmit, watch, formState: { errors } } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { cardType: 'DEBIT', cardNetwork: 'VISA', currency: 'USD', creditLimit: '10000' },
  });

  const cardType = watch('cardType');

  const submit = (v: V) => {
    issue.mutate(
      {
        customerId,
        cardType: v.cardType,
        cardNetwork: v.cardNetwork,
        accountId: v.accountId,
        currency: v.currency || 'USD',
        creditLimit: v.creditLimit || undefined,
      },
      { onSuccess: () => navigate(ROUTES.cards) },
    );
  };

  return (
    <div>
      <PageHeader title="Issue new card" back={{ to: ROUTES.cards }} />
      <Card className="max-w-xl">
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit(submit)}>
            <div className="grid gap-4 md:grid-cols-2">
              <FormField label="Card type" required>
                <Select {...register('cardType')}>
                  <option value="DEBIT">Debit</option>
                  <option value="CREDIT">Credit</option>
                </Select>
              </FormField>
              <FormField label="Network" required>
                <Select {...register('cardNetwork')}>
                  <option value="VISA">Visa</option>
                  <option value="MASTERCARD">Mastercard</option>
                  <option value="AMEX">American Express</option>
                </Select>
              </FormField>
            </div>

            {cardType === 'DEBIT' ? (
              <FormField label="Link to account" error={errors.accountId?.message} required>
                <Select {...register('accountId')}>
                  <option value="">Select an account…</option>
                  {accounts.data?.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.accountType} · {a.accountNumber} · {a.currency}
                    </option>
                  ))}
                </Select>
              </FormField>
            ) : (
              <div className="grid gap-4 md:grid-cols-2">
                <FormField label="Credit limit" required>
                  <Input type="number" min="100" step="100" {...register('creditLimit')} />
                </FormField>
                <FormField label="Currency">
                  <Select {...register('currency')}>
                    <option>USD</option>
                    <option>EUR</option>
                    <option>EGP</option>
                  </Select>
                </FormField>
              </div>
            )}

            <Button type="submit" loading={issue.isPending}>Request card</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
