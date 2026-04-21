import { Receipt } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { EmptyState } from '@shared/ui/EmptyState';

export default function PayBillsPage() {
  return (
    <div>
      <PageHeader title="Pay bills" description="Settle utilities, telecom, and merchant invoices." />
      <Card>
        <CardContent>
          <EmptyState
            icon={<Receipt className="h-5 w-5" />}
            title="No saved billers"
            description="Add a biller to start paying recurring invoices straight from your account."
          />
        </CardContent>
      </Card>
    </div>
  );
}

