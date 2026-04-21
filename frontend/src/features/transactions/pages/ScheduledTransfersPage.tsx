import { CalendarClock } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { EmptyState } from '@shared/ui/EmptyState';

export default function ScheduledTransfersPage() {
  return (
    <div>
      <PageHeader title="Scheduled transfers" description="Automate recurring payments and one-off future transfers." />
      <Card>
        <CardContent>
          <EmptyState
            icon={<CalendarClock className="h-5 w-5" />}
            title="No scheduled transfers"
            description="Create a recurring or future-dated transfer from any account detail page."
          />
        </CardContent>
      </Card>
    </div>
  );
}

