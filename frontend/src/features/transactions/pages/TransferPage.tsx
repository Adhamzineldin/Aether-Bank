import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ArrowRight, Search, CheckCircle2, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';
import { Card, CardContent } from '@shared/ui/Card';
import { FormField } from '@shared/ui/FormField';
import { Input } from '@shared/ui/Input';
import { Select } from '@shared/ui/Select';
import { Button } from '@shared/ui/Button';
import { PageHeader } from '@shared/ui/PageHeader';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { useAuthStore } from '@stores/authStore';
import { ROUTES } from '@app/routes';
import { useMyAccounts } from '@features/accounts/hooks';
import { accountsApi, type Account } from '@features/accounts/api';
import { useTransfer } from '../hooks';
import type { TransferRequest, TransactionType, UUID, Decimal } from '@veld/types';

/**
 * Destination field accepts either an account number (e.g. `DEMO-CHK-USD-001`)
 * or a raw UUID. We resolve account numbers via the account-service lookup
 * endpoint before submitting — nobody should ever type a UUID in production.
 */
const schema = z.object({
  sourceAccountId: z.string().uuid('Choose a source account'),
  destination: z.string().min(4, 'Account number or IBAN required'),
  amount: z.string().refine((v) => Number(v) > 0, 'Amount must be > 0'),
  type: z.enum(['TRANSFER', 'INTERNAL_TRANSFER', 'BILL_PAYMENT']),
});
type Values = z.infer<typeof schema>;

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export default function TransferPage() {
  const userId = useAuthStore((s) => s.user?.id) || '';
  const accounts = useMyAccounts();
  const navigate = useNavigate();
  const [search] = useSearchParams();
  const transfer = useTransfer();

  const [resolvedDest, setResolvedDest] = useState<Account | null>(null);
  const [resolving, setResolving] = useState(false);
  const [resolveError, setResolveError] = useState<string | null>(null);

  const { register, handleSubmit, watch, getValues, formState: { errors } } = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: {
      sourceAccountId: search.get('source') || accounts.data?.[0]?.id || '',
      type: 'TRANSFER',
    },
  });

  const sourceAccountId = watch('sourceAccountId');
  const sourceAccount = accounts.data?.find((a) => a.id === sourceAccountId);

  const resolveDestination = async () => {
    const raw = getValues('destination')?.trim();
    if (!raw) return;

    setResolving(true);
    setResolveError(null);
    setResolvedDest(null);

    try {
      if (UUID_RE.test(raw)) {
        const acct = await accountsApi.get(raw);
        setResolvedDest(acct);
      } else {
        // Strip IBAN prefix if present so `AE07AETHxxxxxxxx` still resolves.
        const needle = raw.startsWith('AE07AETH') ? raw.slice(8).replace(/^0+/, '') : raw;
        const acct = await accountsApi.getByNumber(needle);
        setResolvedDest(acct);
      }
    } catch (e: any) {
      setResolveError(e?.message || 'Account not found');
    } finally {
      setResolving(false);
    }
  };

  const submit = async (v: Values) => {
    let destAccount = resolvedDest;
    if (!destAccount) {
      await resolveDestination();
      destAccount = resolvedDest;
    }
    if (!destAccount) {
      toast.error('Resolve the destination account before sending.');
      return;
    }

    const currency = sourceAccount?.currency || 'USD';
    const payload: TransferRequest = {
      idempotencyKey: crypto.randomUUID(),
      sourceAccountId: v.sourceAccountId as UUID,
      destinationAccountId: destAccount.id as UUID,
      amount: v.amount as Decimal,
      currency,
      sourceCurrency: currency,
      destinationCurrency: destAccount.currency,
      type: v.type as TransactionType,
      initiatedByUserId: userId as UUID,
    } as TransferRequest;

    transfer.mutate(payload, { onSuccess: () => navigate(ROUTES.transactions) });
  };

  return (
    <div>
      <PageHeader
        title="New transfer"
        description="Send money by entering an account number, IBAN, or choosing one of your own accounts."
      />
      <Card className="max-w-2xl">
        <CardContent>
          <form onSubmit={handleSubmit(submit)} className="space-y-4">
            <FormField label="From account" error={errors.sourceAccountId?.message} required>
              <Select {...register('sourceAccountId')}>
                <option value="">Select…</option>
                {accounts.data?.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.accountType} · {a.accountNumber} · {a.currency}
                  </option>
                ))}
              </Select>
            </FormField>

            <FormField
              label="Send to"
              hint="Account number (e.g. DEMO-CHK-USD-001), IBAN, or UUID"
              error={errors.destination?.message}
              required
            >
              <div className="flex gap-2">
                <Input
                  placeholder="DEMO-CHK-USD-001"
                  {...register('destination', {
                    onChange: () => {
                      setResolvedDest(null);
                      setResolveError(null);
                    },
                  })}
                />
                <Button
                  type="button"
                  variant="outline"
                  leftIcon={<Search className="h-4 w-4" />}
                  loading={resolving}
                  onClick={resolveDestination}
                >
                  Look up
                </Button>
              </div>
            </FormField>

            {resolvedDest && (
              <div className="rounded-md border border-emerald-500/30 bg-emerald-500/5 p-3 text-sm">
                <div className="flex items-start gap-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-500 mt-0.5" />
                  <div className="flex-1">
                    <div className="font-medium">
                      {resolvedDest.accountType} · {resolvedDest.accountNumber}
                    </div>
                    <div className="text-muted-fg text-xs">
                      {resolvedDest.currency} account · Status {resolvedDest.status}
                      {resolvedDest.iban ? ` · IBAN ${resolvedDest.iban}` : ''}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {resolveError && (
              <div className="rounded-md border border-red-500/30 bg-red-500/5 p-3 text-sm flex items-start gap-2">
                <AlertCircle className="h-4 w-4 text-red-500 mt-0.5" />
                <span>{resolveError}</span>
              </div>
            )}

            <div className="grid gap-4 md:grid-cols-3">
              <FormField label="Amount" error={errors.amount?.message} required>
                <Input type="number" step="0.01" min="0" placeholder="0.00" {...register('amount')} />
              </FormField>
              <FormField label="Currency">
                <div className="flex h-10 items-center rounded-md border border-border bg-muted/30 px-3 text-sm">
                  {sourceAccount?.currency || 'USD'}
                </div>
              </FormField>
              <FormField label="Type">
                <Select {...register('type')}>
                  <option value="TRANSFER">Transfer</option>
                  <option value="INTERNAL_TRANSFER">Internal</option>
                  <option value="BILL_PAYMENT">Bill payment</option>
                </Select>
              </FormField>
            </div>

            {sourceAccount && (
              <div className="text-xs text-muted-fg">
                Available on source: <CurrencyDisplay amount={sourceAccount.balance} currency={sourceAccount.currency} />
              </div>
            )}

            <Button
              type="submit"
              loading={transfer.isPending}
              rightIcon={<ArrowRight className="h-4 w-4" />}
            >
              Send transfer
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
