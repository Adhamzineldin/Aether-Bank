import { Link } from 'react-router-dom';
import { ScrollText, Plus } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Button } from '@shared/ui/Button';
import { EmptyState } from '@shared/ui/EmptyState';
import { Card, CardContent } from '@shared/ui/Card';
import { Skeleton } from '@shared/ui/Skeleton';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { Badge } from '@shared/ui/Badge';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { ROUTES } from '@app/routes';
import { formatDate } from '@shared/utils/date';
import { useMyLoans } from '../hooks';

export default function LoansListPage() {
  const { data, isLoading } = useMyLoans();

  return (
    <div>
      <PageHeader
        title="Loans"
        description="View and manage your loan applications and active loans."
        actions={
          <Link to={ROUTES.applyLoan}>
            <Button leftIcon={<Plus className="h-4 w-4" />}>Apply for loan</Button>
          </Link>
        }
      />
      <Card>
        <CardContent>
          {isLoading ? (
            <Skeleton className="h-32" />
          ) : !data?.length ? (
            <EmptyState
              icon={<ScrollText className="h-5 w-5" />}
              title="No loans yet"
              description="Apply for a personal, auto, mortgage, or business loan to get started."
              action={<Link to={ROUTES.applyLoan}><Button>Apply</Button></Link>}
            />
          ) : (
            <Table>
              <THead>
                <tr>
                  <TH>Loan #</TH>
                  <TH>Type</TH>
                  <TH>Requested</TH>
                  <TH>Status</TH>
                  <TH>Submitted</TH>
                </tr>
              </THead>
              <TBody>
                {data.map((l: any) => (
                  <TR key={l.id}>
                    <TD className="font-mono text-xs">{l.loanNumber || l.id?.slice(0, 8)}</TD>
                    <TD>{l.loanType}</TD>
                    <TD><CurrencyDisplay amount={l.requestedAmount} currency={l.currency || 'USD'} /></TD>
                    <TD>
                      <Badge tone={l.applicationStatus === 'APPROVED' ? 'success' : l.applicationStatus === 'REJECTED' ? 'danger' : 'warning'}>
                        {l.applicationStatus}
                      </Badge>
                    </TD>
                    <TD>{l.submittedAt ? formatDate(l.submittedAt) : '—'}</TD>
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
