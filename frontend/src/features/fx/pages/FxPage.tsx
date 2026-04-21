import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Globe2 } from 'lucide-react';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { Select } from '@shared/ui/Select';
import { Button } from '@shared/ui/Button';
import { Alert } from '@shared/ui/Alert';
import { CURRENCIES } from '@shared/constants/enums';

const schema = z.object({ sourceCurrency: z.string(), destinationCurrency: z.string() });
type V = z.infer<typeof schema>;

export default function FxPage() {
  const [submitted, setSubmitted] = useState<V | null>(null);
  const { register, handleSubmit } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { sourceCurrency: 'USD', destinationCurrency: 'EUR' },
  });

  return (
    <div>
      <PageHeader title="FX exchange" description="Get live exchange rates and convert between currencies." />
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardContent>
            <form onSubmit={handleSubmit(setSubmitted)} className="space-y-4">
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
            {!submitted ? (
              <p className="text-sm text-muted-fg">Pick a currency pair and submit to view the live rate.</p>
            ) : (
              <Alert tone="info">
                FX rate service is not yet deployed. Requested pair: {submitted.sourceCurrency} → {submitted.destinationCurrency}.
              </Alert>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
