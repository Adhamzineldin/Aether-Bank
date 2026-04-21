import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Card, CardContent } from '@shared/ui/Card';
import { FormField } from '@shared/ui/FormField';
import { Input } from '@shared/ui/Input';
import { Select } from '@shared/ui/Select';
import { Button } from '@shared/ui/Button';
import { PageHeader } from '@shared/ui/PageHeader';
import { ACCOUNT_TYPES, CURRENCIES } from '@shared/constants/enums';
import { ROUTES } from '@app/routes';
import { useAuthStore } from '@stores/authStore';
import { useOpenAccount } from '../hooks';
import type { OpenAccountInput } from '../api';
import type { AccountType } from '@veld/types';

const schema = z.object({
  accountType: z.enum(ACCOUNT_TYPES),
  currency: z.string().min(3),
  initialDeposit: z.string().optional(),
});
type Values = z.infer<typeof schema>;

export default function OpenAccountPage() {
  const userId = useAuthStore((s) => s.user?.id) || '';
  const navigate = useNavigate();
  const open = useOpenAccount();
  const { register, handleSubmit, formState: { errors } } = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { accountType: 'CHECKING', currency: 'USD' },
  });

  const submit = (v: Values) => {
    const payload: OpenAccountInput = {
      customerId: userId,
      accountType: v.accountType as AccountType,
      currency: v.currency,
      initialDeposit: v.initialDeposit || undefined,
    };
    open.mutate(payload, { onSuccess: (res) => navigate(ROUTES.account(res.id)) });
  };

  return (
    <div>
      <PageHeader title="Open new account" back={{ to: ROUTES.accounts }} />
      <Card className="max-w-xl">
        <CardContent>
          <form onSubmit={handleSubmit(submit)} className="space-y-4">
            <FormField label="Account type" error={errors.accountType?.message} required>
              <Select {...register('accountType')}>
                {ACCOUNT_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </Select>
            </FormField>
            <FormField label="Currency" error={errors.currency?.message} required>
              <Select {...register('currency')}>
                {CURRENCIES.map((c) => <option key={c} value={c}>{c}</option>)}
              </Select>
            </FormField>
            <FormField label="Initial deposit" hint="Optional. Funded from external source.">
              <Input type="number" step="0.01" min="0" placeholder="0.00" {...register('initialDeposit')} />
            </FormField>
            <Button type="submit" loading={open.isPending}>Open account</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
