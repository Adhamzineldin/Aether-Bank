import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowRight, Globe2 } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { useVeld } from '@shared/hooks/useVeld';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { Select } from '@shared/ui/Select';
import { Button } from '@shared/ui/Button';
import { Skeleton } from '@shared/ui/Skeleton';
import { CURRENCIES } from '@shared/constants/enums';

const schema = z.object({ sourceCurrency: z.string(), destinationCurrency: z.string() });
type V = z.infer<typeof schema>;

export default function FxPage() {
  const veld = useVeld();
  const [params, setParams] = useState<V | null>(null);
  const { register, handleSubmit } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { sourceCurrency: 'USD', destinationCurrency: 'EUR' },
  });

  const rate = useQuery({
    queryKey: ['fx', params?.sourceCurrency, params?.destinationCurrency],
    enabled: !!params,
    queryFn: () =>
      veld.fx.getExchangeRate({
        sourceCurrency: params!.sourceCurrency,
        destinationCurrency: params!.destinationCurrency,
      } as any),
  });

  return (
    <div>
      <PageHeader title="FX exchange" description="Get live exchange rates and convert between currencies." />
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardContent>
            <form onSubmit={handleSubmit(setParams)} className="space-y-4">
              <FormField label="From">
                <Select {...register('sourceCurrency')}>{CURRENCIES.map((c) => <option key={c}>{c}</option>)}</Select>
              </FormField>
              <FormField label="To">
                <Select {...register('destinationCurrency')}>{CURRENCIES.map((c) => <option key={c}>{c}</option>)}</Select>
              </FormField>
              <Button type="submit" leftIcon={<Globe2 className="h-4 w-4" />}>Get rate</Button>
            </form>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="min-h-[200px]">
            {!params ? (
              <p className="text-sm text-muted-fg">Pick a currency pair and submit to view the live rate.</p>
            ) : rate.isLoading ? (
              <Skeleton className="h-20" />
            ) : !rate.data ? (
              <p className="text-sm text-muted-fg">No rate found.</p>
            ) : (
              <div>
                <p className="text-xs uppercase text-muted-fg tracking-wider">Live rate</p>
                <p className="mt-2 text-3xl font-bold flex items-center gap-3">
                  1 {rate.data.sourceCurrency} <ArrowRight className="h-5 w-5 text-muted-fg" /> {rate.data.exchangeRate} {rate.data.destinationCurrency}
                </p>
                <p className="mt-2 text-xs text-muted-fg">As of {new Date(String(rate.data.timestamp)).toLocaleString()}</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

