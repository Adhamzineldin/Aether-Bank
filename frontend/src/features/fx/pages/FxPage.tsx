import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Globe2, ArrowRight, RefreshCcw } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { Select } from '@shared/ui/Select';
import { Input } from '@shared/ui/Input';
import { Button } from '@shared/ui/Button';
import { Skeleton } from '@shared/ui/Skeleton';
import { CURRENCIES } from '@shared/constants/enums';
import { formatDateTime } from '@shared/utils/date';
import { fxApi, type FxRateResponse } from '../api';

const schema = z.object({
  sourceCurrency: z.string(),
  destinationCurrency: z.string(),
  amount: z.string().optional(),
});
type V = z.infer<typeof schema>;

export default function FxPage() {
  const { register, handleSubmit, watch } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { sourceCurrency: 'USD', destinationCurrency: 'EUR', amount: '100' },
  });

  const rate = useMutation<FxRateResponse, Error, V>({
    mutationFn: (v) => fxApi.getRate(v.sourceCurrency, v.destinationCurrency),
  });

  const amount = Number(watch('amount') || 0);
  const convertedAmount = rate.data
    ? (amount * Number(rate.data.exchangeRate)).toFixed(2)
    : null;

  return (
    <div>
      <PageHeader
        title="FX exchange"
        description="Get live exchange rates and convert between currencies."
      />
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardContent>
            <form onSubmit={handleSubmit((v) => rate.mutate(v))} className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <FormField label="From">
                  <Select {...register('sourceCurrency')}>
                    {CURRENCIES.map((c) => <option key={c}>{c}</option>)}
                  </Select>
                </FormField>
                <FormField label="To">
                  <Select {...register('destinationCurrency')}>
                    {CURRENCIES.map((c) => <option key={c}>{c}</option>)}
                  </Select>
                </FormField>
              </div>
              <FormField label="Amount" hint="Optional — we'll show the converted value when rate returns.">
                <Input type="number" min="0" step="0.01" {...register('amount')} />
              </FormField>
              <Button
                type="submit"
                leftIcon={rate.isPending ? <RefreshCcw className="h-4 w-4 animate-spin" /> : <Globe2 className="h-4 w-4" />}
                loading={rate.isPending}
              >
                Get live rate
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="min-h-[200px]">
            {rate.isPending ? (
              <Skeleton className="h-32" />
            ) : rate.isError ? (
              <div className="rounded-md border border-red-500/30 bg-red-500/5 p-3 text-sm text-red-500">
                {rate.error.message}
              </div>
            ) : rate.data ? (
              <div className="space-y-4">
                <div className="flex items-center gap-3 text-2xl font-semibold">
                  <span>{rate.data.sourceCurrency}</span>
                  <ArrowRight className="h-5 w-5 text-muted-fg" />
                  <span>{rate.data.destinationCurrency}</span>
                </div>
                <div>
                  <div className="text-xs uppercase tracking-wide text-muted-fg">Rate</div>
                  <div className="text-3xl font-mono font-semibold">
                    {Number(rate.data.exchangeRate).toFixed(6)}
                  </div>
                </div>
                {convertedAmount && amount > 0 && (
                  <div>
                    <div className="text-xs uppercase tracking-wide text-muted-fg">Converted</div>
                    <div className="text-lg">
                      {amount.toFixed(2)} {rate.data.sourceCurrency} = <span className="font-semibold">{convertedAmount} {rate.data.destinationCurrency}</span>
                    </div>
                  </div>
                )}
                <div className="text-xs text-muted-fg">As of {formatDateTime(rate.data.timestamp)}</div>
              </div>
            ) : (
              <p className="text-sm text-muted-fg">
                Pick a currency pair and submit to view the live rate.
              </p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
