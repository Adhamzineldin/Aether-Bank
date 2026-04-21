import { Store } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { EmptyState } from '@shared/ui/EmptyState';

export default function MerchantsPage() {
  return (
    <div>
      <PageHeader title="Merchants" description="Trusted merchants for one-tap payments." />
      <Card>
        <CardContent>
          <EmptyState
            icon={<Store className="h-5 w-5" />}
            title="No saved merchants"
            description="Merchants you transact with frequently will appear here for fast checkout."
          />
        </CardContent>
      </Card>
    </div>
  );
}

