import { useState } from 'react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { CheckoutContainer } from '../components/CheckoutContainer';
import { Alert } from '@shared/ui/Alert';
import { CheckCircle2 } from 'lucide-react';

export default function CheckoutPage() {
  const [done, setDone] = useState<{ id?: string } | null>(null);

  return (
    <div>
      <PageHeader title="Checkout" description="Secure payment powered by Aether's shared payment gateway." />
      <div className="grid gap-6 lg:grid-cols-[1fr,360px]">
        <Card>
          <CardContent>
            {done ? (
              <Alert tone="success" title="Payment successful">
                Reference: <code className="font-mono text-xs">{done.id || '—'}</code>
              </Alert>
            ) : (
              <CheckoutContainer
                amount={49.99}
                currency="USD"
                description="Aether demo order #DEMO-1"
                onSuccess={(r) => setDone({ id: r.transactionId })}
              />
            )}
          </CardContent>
        </Card>
        <Card>
          <CardContent className="space-y-3">
            <h3 className="text-sm font-semibold">Order summary</h3>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-fg">Subtotal</span>
              <span>$49.99</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-fg">Tax</span>
              <span>$0.00</span>
            </div>
            <div className="flex items-center justify-between border-t border-border pt-2 text-base font-semibold">
              <span>Total</span>
              <span>$49.99</span>
            </div>
            <p className="flex items-center gap-2 text-xs text-muted-fg">
              <CheckCircle2 className="h-3.5 w-3.5 text-success-600" /> Secured with end-to-end encryption.
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

