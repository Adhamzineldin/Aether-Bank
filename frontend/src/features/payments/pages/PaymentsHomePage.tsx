import { Link } from 'react-router-dom';
import { CreditCard, Receipt, Store } from 'lucide-react';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { ROUTES } from '@app/routes';

const links = [
  { to: ROUTES.checkout, icon: CreditCard, title: 'Checkout demo', desc: 'Run the shared payment gateway component end-to-end.' },
  { to: ROUTES.payBills, icon: Receipt, title: 'Pay bills', desc: 'Pay utility, telecom and government bills.' },
  { to: ROUTES.merchants, icon: Store, title: 'Merchants', desc: 'Saved merchants for one-tap checkout.' },
];

export default function PaymentsHomePage() {
  return (
    <div>
      <PageHeader title="Payments" description="Send money, settle bills, and check out at merchants." />
      <div className="grid gap-4 md:grid-cols-3">
        {links.map(({ to, icon: Icon, title, desc }) => (
          <Link key={to} to={to}>
            <Card className="p-5 transition-colors hover:border-primary/50">
              <CardContent className="p-0 flex gap-4">
                <span className="grid h-10 w-10 place-items-center rounded-xl bg-primary/10 text-primary">
                  <Icon className="h-5 w-5" />
                </span>
                <div>
                  <p className="font-semibold">{title}</p>
                  <p className="text-sm text-muted-fg">{desc}</p>
                </div>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}

