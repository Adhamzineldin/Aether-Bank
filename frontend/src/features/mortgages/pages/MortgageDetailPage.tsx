import { useParams } from 'react-router-dom';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Stat } from '@shared/ui/Stat';
import { Badge } from '@shared/ui/Badge';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { ROUTES } from '@app/routes';
import { formatDate } from '@shared/utils/date';
import { useMortgage, useMortgageSchedule } from '../hooks';

export default function MortgageDetailPage() {
  const { mortgageId = '' } = useParams();
  const mortgage = useMortgage(mortgageId);
  const schedule = useMortgageSchedule(mortgageId);

  if (mortgage.isLoading || !mortgage.data) return <Skeleton className="h-64" />;
  const m = mortgage.data;

  return (
    <div className="space-y-6">
      <PageHeader title={`Mortgage ${m.mortgageNumber}`} description={m.propertyAddress} back={{ to: ROUTES.mortgages }} />
      <div className="grid gap-4 md:grid-cols-4">
        <Stat label="Outstanding" value={<CurrencyDisplay amount={m.outstandingBalance} />} />
        <Stat label="Monthly payment" value={<CurrencyDisplay amount={m.monthlyPayment} />} />
        <Stat label="Interest rate" value={`${m.interestRate}%`} />
        <Stat label="Status" value={<Badge tone={m.status === 'ACTIVE' ? 'success' : 'warning'}>{m.status}</Badge>} />
      </div>
      <Card>
        <CardContent>
          <h3 className="mb-3 text-sm font-semibold">Repayment schedule</h3>
          {schedule.isLoading ? (
            <Skeleton className="h-32" />
          ) : !schedule.data?.length ? (
            <p className="text-sm text-muted-fg">No schedule available yet.</p>
          ) : (
            <Table>
              <THead>
                <tr><TH>#</TH><TH>Due</TH><TH>Principal</TH><TH>Interest</TH><TH>Total</TH><TH>Status</TH></tr>
              </THead>
              <TBody>
                {schedule.data.map((s) => (
                  <TR key={s.id}>
                    <TD>{s.installmentNumber}</TD>
                    <TD>{formatDate(s.dueDate)}</TD>
                    <TD><CurrencyDisplay amount={s.principalComponent} /></TD>
                    <TD><CurrencyDisplay amount={s.interestComponent} /></TD>
                    <TD className="font-medium"><CurrencyDisplay amount={s.totalAmount} /></TD>
                    <TD><Badge tone={s.installmentStatus === 'PAID' ? 'success' : s.installmentStatus === 'OVERDUE' ? 'danger' : 'warning'}>{s.installmentStatus}</Badge></TD>
                  </TR>
                ))}
              </TBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

