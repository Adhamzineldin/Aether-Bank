import { Link } from 'react-router-dom';
import { Home, Plus } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Button } from '@shared/ui/Button';
import { EmptyState } from '@shared/ui/EmptyState';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { Badge } from '@shared/ui/Badge';
import { ROUTES } from '@app/routes';
import { useMyMortgages } from '../hooks';
import { formatDate } from '@shared/utils/date';

export default function MortgagesListPage() {
  const { data, isLoading } = useMyMortgages();

  return (
    <div>
      <PageHeader
        title="Mortgages"
        description="Track your home loans and payment schedules."
        actions={
          <Link to={ROUTES.applyMortgage}>
            <Button leftIcon={<Plus className="h-4 w-4" />}>Apply</Button>
          </Link>
        }
      />
      <Card>
        <CardContent>
          {isLoading ? (
            <Skeleton className="h-32" />
          ) : !data?.length ? (
            <EmptyState
              icon={<Home className="h-5 w-5" />}
              title="No mortgages yet"
              description="Apply for a mortgage to finance your home purchase."
              action={<Link to={ROUTES.applyMortgage}><Button>Apply</Button></Link>}
            />
          ) : (
            <Table>
              <THead>
                <tr>
                  <TH>Mortgage #</TH>
                  <TH>Property</TH>
                  <TH>Outstanding</TH>
                  <TH>Status</TH>
                  <TH>Start</TH>
                  <TH><span className="sr-only">Actions</span></TH>
                </tr>
              </THead>
              <TBody>
                {data.map((m) => (
                  <TR key={m.id}>
                    <TD className="font-mono text-xs">{m.mortgageNumber}</TD>
                    <TD>{m.propertyAddress}</TD>
                    <TD><CurrencyDisplay amount={m.outstandingBalance} /></TD>
                    <TD><Badge tone={m.status === 'ACTIVE' ? 'success' : 'warning'}>{m.status}</Badge></TD>
                    <TD>{formatDate(m.startDate)}</TD>
                    <TD className="text-right">
                      <Link to={ROUTES.mortgage(m.id)} className="text-primary hover:underline text-sm">View</Link>
                    </TD>
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


