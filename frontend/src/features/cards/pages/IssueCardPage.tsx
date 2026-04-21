import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { toast } from 'sonner';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { FormField } from '@shared/ui/FormField';
import { Select } from '@shared/ui/Select';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';

const schema = z.object({
  cardType: z.enum(['DEBIT', 'CREDIT']),
  cardNetwork: z.enum(['VISA', 'MASTERCARD', 'AMEX']),
  accountId: z.string().uuid().optional(),
});
type V = z.infer<typeof schema>;

export default function IssueCardPage() {
  const { register, handleSubmit } = useForm<V>({ resolver: zodResolver(schema), defaultValues: { cardType: 'DEBIT', cardNetwork: 'VISA' } });

  return (
    <div>
      <PageHeader title="Issue new card" back={{ to: ROUTES.cards }} />
      <Card className="max-w-xl">
        <CardContent>
          <form
            className="space-y-4"
            onSubmit={handleSubmit(() => toast.success('Card request submitted (mock — issue endpoint pending).'))}
          >
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
            <Button type="submit">Request card</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

